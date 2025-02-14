package com.mdrsolutions.records_management.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdrsolutions.records_management.controller.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final Set<WebSocketSession> activeSessions = ConcurrentHashMap.newKeySet();

    private final ObjectMapper objectMapper;
    private final SpringTemplateEngine springTemplateEngine;

    public ChatWebSocketHandler(ObjectMapper objectMapper, SpringTemplateEngine springTemplateEngine) {
        this.objectMapper = objectMapper;
        this.springTemplateEngine = springTemplateEngine;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        activeSessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        Map<String, Object> value = objectMapper.readValue(message.getPayload(), new TypeReference<>() {});
        String userMessage = (String) value.get("chatMessage");
        sendToWSSessions(session, userMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        activeSessions.remove(session);
    }

    private void sendToWSSessions(WebSocketSession currentSession, String userMessage) {
        LocalDateTime now = LocalDateTime.now();
        activeSessions.stream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> {
                    // Determine the style based on the session identity
                    String style = session.equals(currentSession) ? "self" : "other";
                    ChatMessage chatMessage = new ChatMessage(
                            style,
                            currentSession.getPrincipal(),
                            userMessage,
                            now);
                    try {
                        session.sendMessage(new TextMessage(thymeleafMessage(chatMessage)));
                    } catch (IOException e) {
                        LOGGER.debug("Unable to send message to {}", session, e);
                    }
                });
    }

    private String thymeleafMessage(ChatMessage message) {
            Context context = new Context(null, Map.of("message", message));
            return springTemplateEngine.process("ws/chat", Set.of("chat-message"), context);
    }

}