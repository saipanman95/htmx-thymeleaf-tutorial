package com.mdrsolutions.records_management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Transcript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transcriptId;

    @OneToOne
    @JoinColumn(name = "prior_school_id", nullable = false)
    private PriorSchool priorSchool;

    @Lob
    private byte[] pdfContent;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    private BigDecimal cumulativeGPA;

    @Column()
    private LocalDate gradeCompilationDate; // Date when grades were compiled

    @OneToMany(mappedBy = "transcript", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Course> courses = new HashSet<>();

    public Long getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(Long transcriptId) {
        this.transcriptId = transcriptId;
    }

    public PriorSchool getPriorSchool() {
        return priorSchool;
    }

    public void setPriorSchool(PriorSchool priorSchool) {
        this.priorSchool = priorSchool;
    }

    public byte[] getPdfContent() {
        return pdfContent;
    }

    public void setPdfContent(byte[] pdfContent) {
        this.pdfContent = pdfContent;
    }

    public BigDecimal getCumulativeGPA() {
        return cumulativeGPA;
    }

    public void setCumulativeGPA(BigDecimal cumulativeGPA) {
        this.cumulativeGPA = cumulativeGPA;
    }

    public LocalDate getGradeCompilationDate() {
        return gradeCompilationDate;
    }

    public void setGradeCompilationDate(LocalDate gradeCompilationDate) {
        this.gradeCompilationDate = gradeCompilationDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }

    public void addCourse(Course course) {
        courses.add(course);
        course.setTranscript(this);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
        course.setTranscript(null);
    }
}
