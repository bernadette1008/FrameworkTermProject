package com.example.demo.repository;

import com.example.demo.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByAssignmentCode(int assignmentCode);

    List<Question> findByStudentId(String studentId);

    Optional<Question> findByQuestionCode(int questionCode);

    List<Question> findByAssignmentCodeAndStudentId(int assignmentCode, String studentId);
}