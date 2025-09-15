package com.example.demo.repository;

import com.example.demo.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {

    List<Answer> findByQuestionCode(int questionCode);

    List<Answer> findByProfessorId(String professorId);

    List<Answer> findByQuestionCodeOrderByAnswerTimeAsc(int questionCode);
}