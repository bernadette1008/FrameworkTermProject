package com.example.demo.repository;

import com.example.demo.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    List<Assignment> findByCourseCode(String courseCode);

    @Query("SELECT a FROM Assignment a WHERE a.courseCode IN " +
            "(SELECT e.courseCode FROM Enrollment e WHERE e.studentId = :studentId)")
    List<Assignment> findByStudentId(@Param("studentId") String studentId);

    @Query("SELECT a FROM Assignment a WHERE a.courseCode IN " +
            "(SELECT c.courseCode FROM Course c WHERE c.professorId = :professorId)")
    List<Assignment> findByProfessorId(@Param("professorId") String professorId);
}