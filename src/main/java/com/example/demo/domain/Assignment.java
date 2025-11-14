package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignment")
@Data
@Getter
@Setter
@ToString(exclude = {"course", "submissions", "questions"})  // 순환 참조 방지
@EqualsAndHashCode(exclude = {"course", "submissions", "questions"})  // 순환 참조 방지
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int assignmentCode;

    @Column(name = "course_code")
    private String courseCode;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdDate;
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_code", insertable = false, updatable = false)
    private Course course;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Submission> submissions;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Question> questions;

    @Transient
    private boolean isSubmitted;

    @Transient
    private boolean isOverdue;
}