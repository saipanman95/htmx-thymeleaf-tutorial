package com.mdrsolutions.records_management.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record TranscriptDto(
        Long transcriptId,
        Long priorSchoolId,
        Long studentId,
        String priorSchoolName,
        BigDecimal cumulativeGPA,
        LocalDate gradeCompilationDate,
        String fileName,
        byte[] pdfContent,
        Set<CourseDto> courses
) {
}