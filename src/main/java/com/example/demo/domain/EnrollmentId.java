package com.example.demo.domain;

import java.io.Serializable;
import java.util.Objects;

public class EnrollmentId implements Serializable {
    private String studentId;
    private String courseCode;

    public EnrollmentId() {}

    public EnrollmentId(String studentId, String courseCode) {
        this.studentId = studentId;
        this.courseCode = courseCode;
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnrollmentId that = (EnrollmentId) o;
        return Objects.equals(studentId, that.studentId) &&
                Objects.equals(courseCode, that.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseCode);
    }
}