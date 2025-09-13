package com.example.demo.repository;

import com.example.demo.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface studentRepository extends JpaRepository<Student, String> {

    Student findBySid(String sid);             // Sid 필드와 일치
    boolean existsBySid(String sid);
    Student findBySidAndPassword(String sid, String password);
}