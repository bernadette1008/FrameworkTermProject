package com.example.demo.repository;

import com.example.demo.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    Student findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
    Student findByStudentIdAndPassword(String studentId, String password);

    // 승인 관련 메서드 추가
    List<Student> findByAllowed(boolean allowed);
}