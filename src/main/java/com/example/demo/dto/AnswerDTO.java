package com.example.demo.dto;

import com.example.demo.domain.Answer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class AnswerDTO {
    // Getters and Setters
    private int answerCode;
    private int questionCode;
    private String professorId;
    private String content;
    private LocalDateTime answerTime;
    private LocalDateTime createdDate;

    // Professor 정보
    private String professorName;

    public AnswerDTO(Answer answer) {
        this.answerCode = answer.getAnswerCode();
        this.questionCode = answer.getQuestionCode();
        this.professorId = answer.getProfessorId();
        this.content = answer.getContent();
        this.answerTime = answer.getAnswerTime();
        this.createdDate = answer.getCreatedDate();

        // Professor 정보 설정 (null 체크)
        if (answer.getProfessor() != null) {
            this.professorName = answer.getProfessor().getName();
        }
    }

}