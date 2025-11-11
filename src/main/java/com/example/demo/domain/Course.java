package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
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
    private String professorId; // 메인 담당교수 ID (수업 생성자, FK)

    // 부교수 ID 리스트 (단순 문자열 컬렉션)
    @ElementCollection
    @CollectionTable(
            name = "course_sub_professors",
            joinColumns = @JoinColumn(name = "course_code")
    )
    @Column(name = "professor_id")
    private List<String> subProfessors = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", insertable = false, updatable = false)
    private Professor professor;  // 메인 교수

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Assignment> assignments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;

    // 추가된 transient 필드들
    @Transient
    private int studentCount;    // 수강생 수

    @Transient
    private int assignmentCount; // 과제 수

    @Transient
    private int subProfessorCount;  // 부교수 수

    public boolean hasSubProfessor(String professorId) {
        if (professorId == null || subProfessors == null) {
            return false;
        }
        return subProfessors.contains(professorId);
    }

}