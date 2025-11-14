package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "question")
@Data
@Setter
@Getter
@ToString(exclude = {"answers", "assignment", "student"})
@EqualsAndHashCode(exclude = {"answers", "assignment", "student"})  // hashCode/equals 순환 참조 방지
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int questionCode;

    @Column(name = "assignment_code")
    private int assignmentCode;

    @Column(name = "student_id")
    private String studentId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime questionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_code", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"question", "hibernateLazyInitializer", "handler"})  // 순환 참조 방지
    private List<Answer> answers;

    @PrePersist
    protected void onCreate() {
        questionTime = LocalDateTime.now();
    }

    public LocalDateTime getCreatedDate() {
        return this.questionTime;
    }

    public LocalDateTime getQuestionTime() {
        return this.questionTime;
    }
}