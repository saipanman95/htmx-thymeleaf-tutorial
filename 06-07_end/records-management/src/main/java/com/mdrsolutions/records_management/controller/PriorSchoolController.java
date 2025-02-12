package com.mdrsolutions.records_management.controller;

import com.mdrsolutions.records_management.controller.dto.MissingDetailsDto;
import com.mdrsolutions.records_management.controller.dto.PriorSchoolDto;
import com.mdrsolutions.records_management.entity.PriorSchool;
import com.mdrsolutions.records_management.entity.Student;
import com.mdrsolutions.records_management.service.CheckStudentMissingFieldService;
import com.mdrsolutions.records_management.service.PriorSchoolService;
import com.mdrsolutions.records_management.service.StudentService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        List<PriorSchoolDto> priorSchoolDtoList = priorSchoolService.findPriorSchoolDtoListByStudentId(studentId);
        model.addAttribute("priorSchoolDtoList", priorSchoolDtoList);
        model.addAttribute("studentId", studentId);
        return "priorSchool/prior-schools :: prior-schools";
    }

    @GetMapping("/student/{studentId}/add")
    public String showAddPriorSchoolForm(@PathVariable Long studentId, Model model) {
        PriorSchoolDto priorSchoolDto = priorSchoolService.initializePriorSchoolDto(studentId);
        model.addAttribute("priorSchoolDto", priorSchoolDto);
        model.addAttribute("studentId", studentId);
        model.addAttribute("editSchool", false);

        return "priorSchool/add-edit-prior-school :: prior-school-form";
    }

    @GetMapping("/school/{id}/edit")
    public String showEditPriorSchoolForm(@PathVariable Long id, Model model) {
        PriorSchoolDto priorSchoolDto = priorSchoolService.getPriorSchoolDtoById(id);
        model.addAttribute("priorSchoolDto", priorSchoolDto);
        model.addAttribute("studentId", priorSchoolDto.studentId());
        model.addAttribute("editSchool", true);

        return "priorSchool/add-edit-prior-school :: prior-school-form";
    }

    @PostMapping("/student/{studentId}/save")
    @HxRequest
    public View savePriorSchool(@PathVariable Long studentId,
                                @ModelAttribute PriorSchoolDto priorSchoolDto,
                                Model model) {
        LOGGER.info("savePriorSchool(...) - studentId: {}", studentId);

        PriorSchoolDto psDto = priorSchoolService.savePriorSchool(priorSchoolDto, studentId);

        Student student = studentService.getStudentById(studentId);

        MissingDetailsDto missingDetailsDto = missingFieldService.checkForMissingFields(student);

        model.addAttribute("student", student);
        model.addAttribute("priorSchoolDto", psDto);
        model.addAttribute("missingDetailsCount", missingDetailsDto.getMissingCount());
        model.addAttribute("missingDetailsList", missingDetailsDto.getMissingFields());
        model.addAttribute("studentId", studentId);

        return FragmentsRendering
                .with("priorSchool/prior-school-table-row :: prior-school")
                .fragment("student/mark-for-review :: mark-for-review-info")
                .build();
    }

    @PutMapping("/school/{priorSchoolId}/update")
    @HxRequest
    public View updatePriorSchool(@PathVariable Long priorSchoolId,
                                                      @ModelAttribute PriorSchoolDto priorSchoolDto,
                                                      Model model) {
        LOGGER.info("updatePriorSchool(...) - priorSchoolId: {}", priorSchoolId);

        Pair<Student, PriorSchoolDto> studentPriorSchoolPair = priorSchoolService.savePriorSchoolWith(priorSchoolDto, priorSchoolDto.studentId());
        MissingDetailsDto missingDetailsDto = missingFieldService.checkForMissingFields(studentPriorSchoolPair.getFirst());

        // Refresh the list
        model.addAttribute("student", studentPriorSchoolPair.getFirst());
        model.addAttribute("priorSchoolDto", studentPriorSchoolPair.getSecond());
        model.addAttribute("studentId", priorSchoolDto.studentId());
        model.addAttribute("missingDetailsCount", missingDetailsDto.getMissingCount());
        model.addAttribute("missingDetailsList", missingDetailsDto.getMissingFields());

        System.out.println("rowId " + priorSchoolDto.priorSchoolId());
        return FragmentsRendering
                .with("priorSchool/prior-school-table-row :: prior-school")
                .fragment("student/mark-for-review :: mark-for-review-info")
                .build();
    }

    @PostMapping("/student/{studentId}/priorSchools/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderPriorSchools(@PathVariable Long studentId,
                                      @RequestParam("priorSchoolIds[]") List<Long> priorSchoolIds) throws InterruptedException {
        LOGGER.info("reorderPriorSchools(...) - studentId: {}", studentId);

        Thread.sleep(500);//artificial latency
        priorSchoolService.updatePriorSchoolOrder(priorSchoolIds, studentId);

    }

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
