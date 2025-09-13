package com.example.demo.repository;

import com.example.demo.domain.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface professorRepository extends JpaRepository<Professor, String> {

    Professor findByPid(String pid);             // Pid 필드와 일치
    boolean existsByPid(String pid);
    Professor findByPidAndPassword(String pid, String password);
}