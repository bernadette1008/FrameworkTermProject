package com.example.demo.repository;

import com.example.demo.domain.Assignment;
import com.example.demo.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCourse(Course course);

    @Query("SELECT a FROM Assignment a WHERE a.course.courseId IN " +
            "(SELECT sc.course.courseId FROM StudentCourse sc WHERE sc.student.sid = :studentId)")
    List<Assignment> findByStudentId(@Param("studentId") String studentId);

    @Query("SELECT a FROM Assignment a WHERE a.course.professor.pid = :professorId")
    List<Assignment> findByProfessorId(@Param("professorId") String professorId);
}