package com.mdrsolutions.records_management.controller;

import com.mdrsolutions.records_management.controller.dto.TranscriptDto;
import com.mdrsolutions.records_management.entity.PriorSchool;
import com.mdrsolutions.records_management.entity.Transcript;
import com.mdrsolutions.records_management.service.PriorSchoolService;
import com.mdrsolutions.records_management.service.TranscriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/transcripts")
public class TranscriptController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptController.class);

    private final TranscriptService transcriptService;
    private final PriorSchoolService priorSchoolService;

    public TranscriptController(TranscriptService transcriptService, PriorSchoolService priorSchoolService) {
        this.transcriptService = transcriptService;
        this.priorSchoolService = priorSchoolService;
    }

    @GetMapping("/priorSchool/{priorSchoolId}")
    public String getTranscriptsByPriorSchoolId(@PathVariable Long priorSchoolId, Model model) {
        PriorSchool priorSchool = priorSchoolService.getPriorSchoolById(priorSchoolId);
        TranscriptDto transcriptDto = transcriptService.getTranscriptDtoByPriorSchoolId(priorSchoolId, priorSchool.getStudent().getStudentId());

        model.addAttribute("transcriptDto", transcriptDto);

        return "transcripts/transcript-list :: transcript-collection";
    }

    @GetMapping("/student/{studentId}")
    public String getTranscriptsByStudentId(@PathVariable Long studentId, Model model) {
        List<TranscriptDto> transcriptDtoList = transcriptService.getTranscriptDtoListByStudentId(studentId);

        model.addAttribute("transcriptDtoList", transcriptDtoList);

        return "transcripts/transcript-list :: transcript-collection";
    }


    @GetMapping("/upload/form/student/{studentId}")
    public String showAddEditForm(@PathVariable Long studentId,
                                  @RequestParam(required = false) Long transcriptId,
                                  Model model) {
        LOGGER.info("TranscriptController.showAddEditForm(...) - studentId: {}, transcriptId: {}", studentId, transcriptId);

        // Fetch or create a TranscriptDto
        TranscriptDto transcriptDto = transcriptId != null
                ? transcriptService.getTranscriptDto(transcriptId, studentId)
                : transcriptService.createEmptyTranscriptDto(studentId);

        List<PriorSchool> priorSchoolList = priorSchoolService.findPriorSchoolsByStudentId(studentId);

        LOGGER.info("transcriptDto == {}", transcriptDto);
        model.addAttribute("priorSchools", priorSchoolList);
        model.addAttribute("transcriptDto", transcriptDto);
        model.addAttribute("isEdit", transcriptId != null); // Flag for Thymeleaf
        model.addAttribute("studentId", studentId);

        return "transcripts/upload-transcript-form :: upload-transcript-form";
    }

    @PostMapping
    public String saveTranscript(@ModelAttribute Transcript transcript, Model model) {
        if (transcript.getGradeCompilationDate() == null) {
            transcript.setGradeCompilationDate(java.time.LocalDate.now());
        }
        Transcript savedTranscript = transcriptService.saveTranscript(transcript);
        model.addAttribute("transcripts", savedTranscript);
        model.addAttribute("message", "Transcript saved successfully!");
        return "transcripts/transcript-list :: transcript-collection";
    }

    @PostMapping("/student/{studentId}/upload")
    public View uploadTranscript(@PathVariable Long studentId,
                                 @RequestParam Long priorSchoolId,
                                 @RequestParam MultipartFile transcriptFile,
                                 @RequestParam(required = false) BigDecimal cumulativeGPA,// may need to eliminate this line
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate gradeCompilationDate,
                                 Model model) {
        LOGGER.info("uploadTranscript(...) - studentId:{}, priorSchoolId:{}", studentId, priorSchoolId);
        // Delegate logic to the service
        TranscriptDto transcriptDto = transcriptService.uploadAndReturnTranscriptDto(
                studentId, priorSchoolId, transcriptFile, cumulativeGPA, gradeCompilationDate
        );

        LOGGER.info("transcriptDto after save => {}", transcriptDto);
        model.addAttribute("transcriptDto", transcriptDto);
        return FragmentsRendering.with("transcripts/transcript-list-item :: transcript")
                .fragment("empty-fragment :: empty")
                .build();
    }

    @DeleteMapping("/{transcriptId}")
    public String deleteTranscript(@PathVariable Long transcriptId, Model model) {
        transcriptService.deleteTranscript(transcriptId);
        model.addAttribute("message", "Transcript deleted successfully!");
        return "transcripts/transcript-list :: transcript-collection";
    }
}
