package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "submission")
@Data
@Setter
@Getter
public class Submission {
    @Id
    private String submissionCode;   // 제출물코드(PK)

    @Column(name = "assignment_code")
    private String assignmentCode;   // 과제코드(FK)

    @Column(name = "student_id")
    private String studentId;        // 학번(FK)

    private LocalDateTime submissionTime;    // 제출시간

    @Column(columnDefinition = "TEXT")
    private String content;          // 제출내용

    private Integer score;           // 점수

    @Column(columnDefinition = "TEXT")
    private String feedback;         // 피드백

    private LocalDateTime lastModifiedDate;  // 최종 수정일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_code", insertable = false, updatable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    @PrePersist
    protected void onCreate() {
        submissionTime = LocalDateTime.now();
        lastModifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}