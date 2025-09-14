package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Setter
@Getter
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime dueDate;
    private LocalDateTime createdDate;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Submission> submissions;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Question> questions;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}