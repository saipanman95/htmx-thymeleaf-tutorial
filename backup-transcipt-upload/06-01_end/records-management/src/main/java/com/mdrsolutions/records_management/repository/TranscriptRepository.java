package com.mdrsolutions.records_management.repository;

import com.mdrsolutions.records_management.controller.dto.TranscriptDto;
import com.mdrsolutions.records_management.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TranscriptRepository extends JpaRepository<Transcript, Long> {
    Optional<Transcript> findByPriorSchool_Id(Long priorSchoolId);

    List<Transcript> findByPriorSchool_Student_StudentId(Long studentId);

}
