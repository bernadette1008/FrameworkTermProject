package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "submission")
@Data
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int submissionCode;

    @Column(name = "assignment_code")
    private int assignmentCode;

    @Column(name = "student_id")
    private String studentId;

    private LocalDateTime submissionTime;
    @Column(columnDefinition = "TEXT")
    private String content;
    private Integer score;
    @Column(columnDefinition = "TEXT")
    private String feedback;
    private LocalDateTime lastModifiedDate;

    // 파일 업로드 관련 필드 추가
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_code", insertable = false, updatable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;


}
