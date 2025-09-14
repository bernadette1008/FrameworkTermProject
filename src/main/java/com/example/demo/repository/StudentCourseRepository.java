package com.example.demo.repository;

import com.example.demo.domain.StudentCourse;
import com.example.demo.domain.Student;
import com.example.demo.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    List<StudentCourse> findByStudent(Student student);

    List<StudentCourse> findByCourse(Course course);

    boolean existsByStudentAndCourse(Student student, Course course);

    void deleteByStudentAndCourse(Student student, Course course);
}