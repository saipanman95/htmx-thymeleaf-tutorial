package com.mdrsolutions.records_management.controller.dto;

import com.mdrsolutions.records_management.entity.Transcript;

import java.util.stream.Collectors;

public class TranscriptMapper {

    public static TranscriptDto toDto(Transcript transcript, Long studentId) {
        return new TranscriptDto(
                transcript.getTranscriptId(),
                transcript.getPriorSchool().getId(),
                studentId,
                transcript.getPriorSchool().getSchoolName(),
                transcript.getCumulativeGPA(),
                transcript.getGradeCompilationDate(),
                transcript.getFileName(),
                transcript.getPdfContent(),
                transcript.getCourses().stream()
                        .map(course -> new CourseDto(
                                course.getCourseId(),
                                course.getCourseNumber(),
                                course.getCourseTitle(),
                                course.getCreditHours(),
                                course.getGrade(),
                                course.getDateTaken()
                        ))
                        .collect(Collectors.toSet())
        );
    }
}