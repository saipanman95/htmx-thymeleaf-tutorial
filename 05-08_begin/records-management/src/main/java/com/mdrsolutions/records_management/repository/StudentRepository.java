package com.mdrsolutions.records_management.repository;

import com.mdrsolutions.records_management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByGuardiansPersonId(Long personId);
    @Query("SELECT s FROM Student s JOIN s.guardians g WHERE g.personId = :personId")
    List<Student> findStudentsByPersonId(@Param("personId") Long personId);

}
