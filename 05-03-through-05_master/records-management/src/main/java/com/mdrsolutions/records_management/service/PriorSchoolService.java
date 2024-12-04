package com.mdrsolutions.records_management.service;

import com.mdrsolutions.records_management.entity.PriorSchool;
import com.mdrsolutions.records_management.entity.Student;
import com.mdrsolutions.records_management.repository.PriorSchoolRepository;
import com.mdrsolutions.records_management.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PriorSchoolService {

    private final Logger LOGGER = LoggerFactory.getLogger(PriorSchoolService.class);

    private final PriorSchoolRepository priorSchoolRepository;
    private final StudentRepository studentRepository;

    public PriorSchoolService(PriorSchoolRepository priorSchoolRepository, StudentRepository studentRepository) {
        this.priorSchoolRepository = priorSchoolRepository;
        this.studentRepository = studentRepository;
    }


    public List<PriorSchool> getPriorSchoolsByStudentId(Long studentId) {
        //getting the list by last attended in Descending order
        return priorSchoolRepository.findByStudentStudentIdOrderByDateLastAttendedDesc(studentId);
    }

    public PriorSchool getPriorSchoolById(Long id) {
        return priorSchoolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Prior School ID"));
    }

    public PriorSchool savePriorSchool(PriorSchool priorSchool) {
        return priorSchoolRepository.save(priorSchool);
    }

    public Pair<Student,PriorSchool> savePriorSchoolWith(PriorSchool priorSchool, Long studentId){
        LOGGER.info("savePriorSchoolWith(...) - studentId = {}", studentId);

        PriorSchool school = savePriorSchool(priorSchool);
        Optional<Student> optionalStudent = studentRepository.findById(studentId);
        Student student = optionalStudent.get();

        student.addPriorSchool(school);

        student = studentRepository.save(student);
        return Pair.of(student, school);
    }
    public void deletePriorSchool(Long id) {
        priorSchoolRepository.deleteById(id);
    }

    public PriorSchool findMostRecentSchool(Long studentId) {
        Optional<PriorSchool> lastPriorSchool = priorSchoolRepository.findMostRecentPriorSchool(studentId);
        return lastPriorSchool.orElse(null);
    }

    public Long findStudentIdByPriorSchoolId(Long priorSchoolId){
        return  priorSchoolRepository.findStudentIdByPriorSchoolId(priorSchoolId);
    }
}
