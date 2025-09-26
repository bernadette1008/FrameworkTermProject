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
    private String fileName;        // 원본 파일명 또는 저장된 파일명
    private String filePath;        // 파일 저장 경로
    private String originalFileName; // 원본 파일명 (사용자가 업로드한 파일명)
    private Long fileSize;          // 파일 크기
    private String fileContentType; // 파일 MIME 타입

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_code", insertable = false, updatable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;
}