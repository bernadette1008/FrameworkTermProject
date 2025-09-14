package com.example.demo.repository;

import com.example.demo.domain.Enrollment;
import com.example.demo.domain.EnrollmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    List<Enrollment> findByStudentId(String studentId);

    List<Enrollment> findByCourseCode(String courseCode);

    boolean existsByStudentIdAndCourseCode(String studentId, String courseCode);

    void deleteByStudentIdAndCourseCode(String studentId, String courseCode);
}