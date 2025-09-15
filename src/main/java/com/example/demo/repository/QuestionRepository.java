package com.example.demo.repository;

import com.example.demo.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {

    List<Question> findByAssignmentCode(int assignmentCode);

    List<Question> findByStudentId(String studentId);

    List<Question> findByAssignmentCodeAndStudentId(int assignmentCode, String studentId);
}