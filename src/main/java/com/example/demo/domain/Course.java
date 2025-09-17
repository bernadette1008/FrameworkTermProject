package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "course")
@Data
@Setter
@Getter
public class Course {
    @Id
    private String courseCode;  // 수업코드(PK)
    private String courseName;  // 수업명

    @Column(name = "professor_id")
    private String professorId; // 담당교수아이디(FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", insertable = false, updatable = false)
    private Professor professor;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Assignment> assignments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;

    // 추가된 transient 필드들
    @Transient
    private int studentCount;    // 수강생 수

    @Transient
    private int assignmentCount; // 과제 수
}