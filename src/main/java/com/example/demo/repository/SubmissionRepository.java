package com.example.demo.repository;

import com.example.demo.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, String> {

    Optional<Submission> findByAssignmentCodeAndStudentId(String assignmentCode, String studentId);

    List<Submission> findByStudentId(String studentId);

    List<Submission> findByAssignmentCode(String assignmentCode);

    boolean existsByAssignmentCodeAndStudentId(String assignmentCode, String studentId);
}