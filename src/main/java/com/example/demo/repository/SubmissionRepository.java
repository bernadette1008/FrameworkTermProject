package com.example.demo.repository;

import com.example.demo.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, String> {

    Optional<Submission> findByAssignmentCodeAndStudentId(int assignmentCode, String studentId);

    List<Submission> findByStudentId(String studentId);

    Submission findBySubmissionCode(int submissionCode);

    List<Submission> findByAssignmentCode(int assignmentCode);

    boolean existsByAssignmentCodeAndStudentId(int assignmentCode, String studentId);
}