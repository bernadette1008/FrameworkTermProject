package com.example.demo.repository;

import com.example.demo.domain.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, String> {

    Professor findByProfessorId(String professorId);
    boolean existsByProfessorId(String professorId);
//    Professor findByProfessorIdAndPassword(String professorId, String password);

    // 승인 관련 메서드 추가
    List<Professor> findByAllowed(boolean allowed);
}