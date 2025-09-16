package com.example.demo.dto;

import com.example.demo.domain.Submission;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class SubmissionDTO {
    // Getters and Setters
    private int submissionCode;
    private int assignmentCode;
    private String studentId;
    private String content;
    private Integer score;
    private String feedback;
    private LocalDateTime submissionTime;
    private LocalDateTime lastModifiedDate;

    // Assignment 정보
    private String assignmentTitle;
    private String courseName;

    public SubmissionDTO(Submission submission) {
        this.submissionCode = submission.getSubmissionCode();
        this.assignmentCode = submission.getAssignmentCode();
        this.studentId = submission.getStudentId();
        this.content = submission.getContent();
        this.score = submission.getScore();
        this.feedback = submission.getFeedback();
        this.submissionTime = submission.getSubmissionTime();
        this.lastModifiedDate = submission.getLastModifiedDate();

        // Assignment 정보 설정 (null 체크)
        if (submission.getAssignment() != null) {
            this.assignmentTitle = submission.getAssignment().getTitle();
            if (submission.getAssignment().getCourse() != null) {
                this.courseName = submission.getAssignment().getCourse().getCourseName();
            }
        }
    }

}