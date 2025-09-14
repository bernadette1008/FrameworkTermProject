package com.example.demo.repository;

import com.example.demo.domain.Course;
import com.example.demo.domain.Professor;
import com.example.demo.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByProfessor(Professor professor);

    @Query("SELECT c FROM Course c JOIN c.studentCourses sc WHERE sc.student = :student")
    List<Course> findByStudent(@Param("student") Student student);

    @Query("SELECT c FROM Course c WHERE c.professor.pid = :professorId")
    List<Course> findByProfessorId(@Param("professorId") String professorId);
}