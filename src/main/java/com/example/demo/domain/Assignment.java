package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignment")
@Data
@Setter
@Getter
public class Assignment {
    @Id
    private String assignmentCode;  // 과제코드(PK)

    @Column(name = "course_code")
    private String courseCode;      // 수업코드(FK)

    private String title;           // 이름

    @Column(columnDefinition = "TEXT")
    private String content;         // 내용

    private LocalDateTime createdDate;  // 생성시간
    private LocalDateTime dueDate;      // 마감시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_code", insertable = false, updatable = false)
    private Course course;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Submission> submissions;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Question> questions;

    // 제출 상태와 기한 초과 여부를 위한 임시 필드들 (DB에 저장되지 않음)
    @Transient
    private boolean isSubmitted;

    @Transient
    private boolean isOverdue;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    // Helper methods
    public boolean isSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }

    public boolean isOverdue() {
        return isOverdue;
    }

    public void setOverdue(boolean overdue) {
        isOverdue = overdue;
    }
}