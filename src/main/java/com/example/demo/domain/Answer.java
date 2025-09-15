package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "answer")
@Data
@Setter
@Getter
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int answerCode;      // 답변코드(PK)

    @Column(name = "question_code")
    private int questionCode;    // 질문코드(FK)

    @Column(name = "professor_id")
    private String professorId;     // 교수아이디(FK)

    @Column(columnDefinition = "TEXT")
    private String content;         // 내용

    private LocalDateTime answerTime;    // 답변시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_code", insertable = false, updatable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", insertable = false, updatable = false)
    private Professor professor;

    @PrePersist
    protected void onCreate() {
        answerTime = LocalDateTime.now();
    }

    // JavaScript와의 호환성을 위한 getter 추가
    public LocalDateTime getCreatedDate() {
        return this.answerTime;
    }

    // 기존 필드명도 유지
    public LocalDateTime getAnswerTime() {
        return this.answerTime;
    }
}