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
public interface CourseRepository extends JpaRepository<Course, String> {

    Course findByCourseCode(String courseCode);
    boolean existsByCourseCode(String courseCode);

    List<Course> findByProfessorId(String professorId);

    @Query(value = """
    SELECT DISTINCT c.* 
    FROM course c
    LEFT JOIN course_sub_professors sp 
        ON c.course_code = sp.course_code
    WHERE c.professor_id = :professorId 
       OR sp.professor_id = :professorId
    """, nativeQuery = true)
    List<Course> findByProfessorOrSubProfessor(@Param("professorId") String professorId);

    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.studentId = :studentId")
    List<Course> findByStudentId(@Param("studentId") String studentId);
}