package com.mdrsolutions.records_management;

import com.mdrsolutions.records_management.entity.PriorSchool;
import com.mdrsolutions.records_management.service.PriorSchoolService;
import com.mdrsolutions.records_management.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.Optional;

@SpringBootTest
public class PriorSchoolServiceTest {

    @Autowired
    private PriorSchoolService priorSchoolService;

    @Autowired
    private StudentService studentService;

    @Test
    public void testFindMostRecentPriorSchool() {
        Long studentId = 1L; // Replace with a valid student ID

        Optional<PriorSchool> mostRecentSchool = priorSchoolService.findMostRecentSchool(studentId);

        if (mostRecentSchool.isPresent()) {
            System.out.println("Most recent prior school for student ID " + studentId + ":");
            System.out.println("School Name: " + mostRecentSchool.get().getSchoolName());
            System.out.println("Date Last Attended: " + mostRecentSchool.get().getDateLastAttended());
        } else {
            System.out.println("No prior schools found for student ID " + studentId);
        }
    }
}
