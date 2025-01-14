package com.mdrsolutions.records_management.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CourseDto(
        Long courseId,
        String courseNumber,
        String courseTitle,
        BigDecimal creditHours,
        String grade,
        LocalDate dateTaken
) {}