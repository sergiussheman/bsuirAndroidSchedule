package com.example.myapplication.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Лол on 05.08.2015.
 */
public class Schedule implements Serializable{
    private List<String> employees = new ArrayList<>();
    private List<Employee> employeeList = new ArrayList<>();
    private List<String> auditories = new ArrayList<>();
    private List<String> weekNumbers = new ArrayList<>();
    private String lessonTime = "";
    private String lessonType = "";
    private String subject = "";
    private String subGroup = "";
    private String studentGroup = "";
    private String note = "";

    public List<String> getEmployees() {
        return employees;
    }

    public void setEmployees(List<String> employees) {
        this.employees = employees;
    }

    public List<String> getAuditories() {
        return auditories;
    }

    public void setAuditories(List<String> auditories) {
        this.auditories = auditories;
    }

    public List<String> getWeekNumbers() {
        return weekNumbers;
    }

    public void setWeekNumbers(List<String> weekNumbers) {
        this.weekNumbers = weekNumbers;
    }

    public String getLessonTime() {
        return lessonTime;
    }

    public void setLessonTime(String lessonTime) {
        this.lessonTime = lessonTime;
    }

    public String getLessonType() {
        return lessonType;
    }

    public void setLessonType(String lessonType) {
        this.lessonType = lessonType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }

    public String getStudentGroup() {
        return studentGroup;
    }

    public void setStudentGroup(String studentGroup) {
        this.studentGroup = studentGroup;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
