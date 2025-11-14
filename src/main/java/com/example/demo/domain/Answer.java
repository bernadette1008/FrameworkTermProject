package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "answer")
@Data
@Setter
@Getter
@ToString(exclude = {"question", "professor"})
@EqualsAndHashCode(exclude = {"question", "professor"})  // hashCode/equals 순환 참조 방지
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "question"})
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int answerCode;

    @Column(name = "question_code")
    private int questionCode;

    @Column(name = "professor_id")
    private String professorId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime answerTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_code", insertable = false, updatable = false)
    @JsonIgnoreProperties({"answers", "hibernateLazyInitializer", "handler"})  // 순환 참조 방지
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Professor professor;

    @PrePersist
    protected void onCreate() {
        answerTime = LocalDateTime.now();
    }

    public LocalDateTime getCreatedDate() {
        return this.answerTime;
    }

    public LocalDateTime getAnswerTime() {
        return this.answerTime;
    }
}