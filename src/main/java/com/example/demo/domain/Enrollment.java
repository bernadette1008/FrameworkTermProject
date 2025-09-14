package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment")
@Data
@Setter
@Getter
@IdClass(EnrollmentId.class)
public class Enrollment {
    @Id
    @Column(name = "student_id")
    private String studentId;    // 학번(FK, 복합PK)

    @Id
    @Column(name = "course_code")
    private String courseCode;   // 수업코드(FK, 복합PK)

    private LocalDateTime enrollmentDate; // 수강신청일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_code", insertable = false, updatable = false)
    private Course course;

    @PrePersist
    protected void onCreate() {
        enrollmentDate = LocalDateTime.now();
    }
}