package com.mdrsolutions.records_management.service;

import com.mdrsolutions.records_management.controller.dto.TranscriptDto;
import com.mdrsolutions.records_management.controller.dto.TranscriptMapper;
import com.mdrsolutions.records_management.entity.PriorSchool;
import com.mdrsolutions.records_management.entity.Transcript;
import com.mdrsolutions.records_management.repository.PriorSchoolRepository;
import com.mdrsolutions.records_management.repository.TranscriptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TranscriptService {

    private final static Logger LOGGER = LoggerFactory.getLogger(TranscriptService.class);

    private final TranscriptRepository transcriptRepository;
    private final PriorSchoolRepository priorSchoolRepository;

    public TranscriptService(TranscriptRepository transcriptRepository, PriorSchoolRepository priorSchoolRepository) {
        this.transcriptRepository = transcriptRepository;
        this.priorSchoolRepository = priorSchoolRepository;
    }

    public Transcript saveTranscript(Transcript transcript) {
        return transcriptRepository.save(transcript);
    }

    public Optional<Transcript> getTranscriptByPriorSchoolId(Long priorSchoolId) {
        return transcriptRepository.findByPriorSchool_Id(priorSchoolId);
    }

    public TranscriptDto getTranscriptDtoByPriorSchoolId(Long priorSchoolId, Long studentId){
        return getTranscriptByPriorSchoolId(priorSchoolId)
                .map(transcript -> TranscriptMapper.toDto(transcript, studentId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid Prior School ID"));
    }

    public TranscriptDto getTranscriptDto(Long transcriptId, Long studentId) {
        Transcript transcript = Optional.of(transcriptId)
                .flatMap(transcriptRepository::findById)
                .orElseThrow();

        return TranscriptMapper.toDto(transcript, studentId);
    }

    public void deleteTranscript(Long transcriptId) {
        transcriptRepository.deleteById(transcriptId);
    }

    public TranscriptDto uploadAndReturnTranscriptDto(Long studentId,
                                                      Long priorSchoolId,
                                                      MultipartFile transcriptFile,
                                                      BigDecimal cumulativeGPA,
                                                      LocalDate gradeCompilationDate) {
        // Handle transcript upload
        uploadTranscript(studentId, priorSchoolId, transcriptFile, cumulativeGPA, gradeCompilationDate);

        // Fetch the transcript DTO to return
        return getTranscriptDtoByPriorSchoolId(priorSchoolId, studentId);
    }

    public void uploadTranscript(Long studentId,
                                 Long priorSchoolId,
                                 MultipartFile transcriptFile,
                                 BigDecimal cumulativeGPA,
                                 LocalDate gradeCompilationDate) {
        // Find the associated Prior School
        PriorSchool priorSchool = priorSchoolRepository.findById(priorSchoolId)
                .orElseThrow(() -> new IllegalArgumentException("Prior School not found with ID: " + priorSchoolId));

        // Save the transcript as a byte array
        byte[] fileContent;
        try {
            fileContent = transcriptFile.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read transcript file content", e);
        }

        LOGGER.info("fileContents: [{}]", fileContent);
        // Create and save the Transcript entity
        Transcript transcript = new Transcript();
        transcript.setPriorSchool(priorSchool);
        transcript.setCumulativeGPA(cumulativeGPA);
        transcript.setGradeCompilationDate(gradeCompilationDate);
        transcript.setPdfContent(fileContent);
        transcript.setFileName(transcriptFile.getOriginalFilename());
        transcript.setContentType(transcriptFile.getContentType());

        transcriptRepository.save(transcript);
    }

    public List<TranscriptDto> getTranscriptDtoListByStudentId(Long studentId) {

        List<TranscriptDto> transcriptDtoList = new ArrayList<>();
        List<Transcript> transcripts = transcriptRepository.findByPriorSchool_Student_StudentId(studentId);

        for(Transcript transcript : transcripts){
            transcriptDtoList.add(TranscriptMapper.toDto(transcript, studentId));
        }

        return transcriptDtoList;
    }

    public TranscriptDto createEmptyTranscriptDto(Long studentId) {
        return new TranscriptDto(
                null,
                null,
                studentId,
                null,
                null,
                null,
                null,
                null,
                 null
        );
    }
}
