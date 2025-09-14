package com.example.demo.repository;

import com.example.demo.domain.Submission;
import com.example.demo.domain.Assignment;
import com.example.demo.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, Student student);

    List<Submission> findByStudent(Student student);

    List<Submission> findByAssignment(Assignment assignment);

    boolean existsByAssignmentAndStudent(Assignment assignment, Student student);
}