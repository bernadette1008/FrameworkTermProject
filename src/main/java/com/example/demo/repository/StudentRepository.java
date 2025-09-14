package com.example.demo.repository;

import com.example.demo.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    Student findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
    Student findByStudentIdAndPassword(String studentId, String password);
}