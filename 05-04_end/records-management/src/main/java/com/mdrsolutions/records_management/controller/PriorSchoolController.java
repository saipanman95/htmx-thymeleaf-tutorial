package com.mdrsolutions.records_management.controller;

import com.mdrsolutions.records_management.controller.dto.MissingDetailsDto;
import com.mdrsolutions.records_management.entity.PriorSchool;
import com.mdrsolutions.records_management.entity.Student;
import com.mdrsolutions.records_management.service.CheckStudentMissingFieldService;
import com.mdrsolutions.records_management.service.PriorSchoolService;
import com.mdrsolutions.records_management.service.StudentService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxView;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/prior-schools")
public class PriorSchoolController {

    public static final Logger LOGGER = LoggerFactory.getLogger(PriorSchoolController.class);
    private final PriorSchoolService priorSchoolService;
    private final StudentService studentService;
    private final CheckStudentMissingFieldService missingFieldService;

    public PriorSchoolController(PriorSchoolService priorSchoolService, StudentService studentService, CheckStudentMissingFieldService missingFieldService) {
        this.priorSchoolService = priorSchoolService;
        this.studentService = studentService;
        this.missingFieldService = missingFieldService;
    }

    @GetMapping("/student/{studentId}")
    public String showPriorSchools(@PathVariable Long studentId, Model model) {
        LOGGER.info("showPriorSchools");
        List<PriorSchool> priorSchools = priorSchoolService.getPriorSchoolsByStudentId(studentId);
        model.addAttribute("priorSchools", priorSchools);
        model.addAttribute("studentId", studentId);

        return "priorSchool/prior-schools :: prior-schools";
    }

    @GetMapping("/student/{studentId}/add")
    public String showAddPriorSchoolForm(@PathVariable Long studentId, Model model) {
        PriorSchool priorSchool = new PriorSchool();
        model.addAttribute("priorSchool", priorSchool);
        model.addAttribute("studentId", studentId);
        model.addAttribute("editSchool", false);

        return "priorSchool/add-edit-prior-school :: prior-school-form";
    }

    @GetMapping("/school/{id}/edit")
    public String showEditPriorSchoolForm(@PathVariable Long id, Model model) {
        PriorSchool priorSchool = priorSchoolService.getPriorSchoolById(id);
        model.addAttribute("priorSchool", priorSchool);
        model.addAttribute("studentId", priorSchool.getStudent().getStudentId());
        model.addAttribute("editSchool", true);

        return "priorSchool/add-edit-prior-school :: prior-school-form";
    }

    @PostMapping("/student/{studentId}/save")
    @HxRequest
    public HtmxView savePriorSchool(@PathVariable Long studentId,
                                        @ModelAttribute PriorSchool priorSchool,
                                        Model model) {
        LOGGER.info("savePriorSchool(...) - studentId: {}", studentId);
        Student student = studentService.getStudentById(studentId);
        priorSchool.setStudent(student);

        PriorSchool ps = priorSchoolService.savePriorSchool(priorSchool);

        student = studentService.getStudentById(studentId);
        MissingDetailsDto missingDetailsDto = missingFieldService.checkForMissingFields(student);

        model.addAttribute("priorSchool", ps);
        model.addAttribute("studentId", studentId);
        model.addAttribute("missingDetailsCount", missingDetailsDto.getMissingCount());
        model.addAttribute("missingDetailsList", missingDetailsDto.getMissingFields());

        return new HtmxView(
                "priorSchool/prior-school-table-row :: prior-school",
                "student/mark-for-review :: mark-for-review-info");
    }

    @PutMapping("/school/{id}/update")
    @HxRequest
    public HtmxView updatePriorSchool(@PathVariable Long id,
                                    @ModelAttribute PriorSchool priorSchool,
                                    Model model) {
        LOGGER.info("updatePriorSchool(...) - priorSchoolId: {}", id);
        PriorSchool existingPriorSchool = priorSchoolService.getPriorSchoolById(id);
        // Update fields
        existingPriorSchool.setSchoolName(priorSchool.getSchoolName());
        existingPriorSchool.setAddress(priorSchool.getAddress());
        existingPriorSchool.setCity(priorSchool.getCity());
        existingPriorSchool.setState(priorSchool.getState());
        existingPriorSchool.setZip(priorSchool.getZip());
        existingPriorSchool.setPhoneNumber(priorSchool.getPhoneNumber());
        existingPriorSchool.setAdministratorFirstName(priorSchool.getAdministratorFirstName());
        existingPriorSchool.setAdministratorLastName(priorSchool.getAdministratorLastName());
        existingPriorSchool.setGpa(priorSchool.getGpa());
        existingPriorSchool.setGradeLevel(priorSchool.getGradeLevel());
        existingPriorSchool.setDateStartedAttending(priorSchool.getDateStartedAttending());
        existingPriorSchool.setDateLastAttended(priorSchool.getDateLastAttended());

        Pair<Student, PriorSchool> studentPriorSchoolPair = priorSchoolService.savePriorSchoolWith(existingPriorSchool, existingPriorSchool.getStudent().getStudentId());

        MissingDetailsDto missingDetailsDto = missingFieldService.checkForMissingFields(studentPriorSchoolPair.getFirst());

        model.addAttribute("priorSchool", studentPriorSchoolPair.getSecond());
        model.addAttribute("missingDetailsCount", missingDetailsDto.getMissingCount());
        model.addAttribute("missingDetailsList", missingDetailsDto.getMissingFields());

        return new HtmxView(
                "priorSchool/prior-school-table-row :: prior-school",
                "student/mark-for-review :: mark-for-review-info");    }

    @DeleteMapping("/delete/{id}")
    public String deletePriorSchool(@PathVariable Long id, Model model) {
        PriorSchool priorSchool = priorSchoolService.getPriorSchoolById(id);
        Long studentId = priorSchool.getStudent().getStudentId();

        priorSchoolService.deletePriorSchool(id);

        // Refresh the list
        List<PriorSchool> priorSchools = priorSchoolService.getPriorSchoolsByStudentId(studentId);
        model.addAttribute("priorSchools", priorSchools);
        model.addAttribute("studentId", studentId);

        return "priorSchool/prior-schools :: prior-schools";
    }
}
