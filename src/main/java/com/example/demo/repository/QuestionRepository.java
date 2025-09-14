package com.example.demo.repository;

import com.example.demo.domain.Question;
import com.example.demo.domain.Assignment;
import com.example.demo.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByAssignment(Assignment assignment);

    List<Question> findByStudent(Student student);

    List<Question> findByAssignmentAndStudent(Assignment assignment, Student student);
}