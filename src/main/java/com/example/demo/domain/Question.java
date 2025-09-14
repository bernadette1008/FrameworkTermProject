package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "question")
@Data
@Setter
@Getter
public class Question {
    @Id
    private String questionCode;    // 질문코드(PK)

    @Column(name = "assignment_code")
    private String assignmentCode;  // 과제코드(FK)

    @Column(name = "student_id")
    private String studentId;       // 학번(FK)

    @Column(columnDefinition = "TEXT")
    private String content;         // 내용

    private LocalDateTime questionTime;  // 질문시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_code", insertable = false, updatable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Answer> answers;

    @PrePersist
    protected void onCreate() {
        questionTime = LocalDateTime.now();
    }
}