package com.example.demo.dto;

import com.example.demo.domain.Question;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class QuestionDTO {
    // Getters and Setters
    private int questionCode;
    private int assignmentCode;
    private String studentId;
    private String content;
    private LocalDateTime questionTime;
    private LocalDateTime createdDate;

    // Assignment 정보
    private String assignmentTitle;
    private String courseName;

    // Answer 정보
    private List<AnswerDTO> answers;

    public QuestionDTO(Question question) {
        this.questionCode = question.getQuestionCode();
        this.assignmentCode = question.getAssignmentCode();
        this.studentId = question.getStudentId();
        this.content = question.getContent();
        this.questionTime = question.getQuestionTime();
        this.createdDate = question.getCreatedDate();

        // Assignment 정보 설정 (null 체크)
        if (question.getAssignment() != null) {
            this.assignmentTitle = question.getAssignment().getTitle();
            if (question.getAssignment().getCourse() != null) {
                this.courseName = question.getAssignment().getCourse().getCourseName();
            }
        }

        // Answers 설정 (null 체크)
        if (question.getAnswers() != null) {
            this.answers = question.getAnswers().stream()
                    .map(AnswerDTO::new)
                    .collect(Collectors.toList());
        }
    }

}