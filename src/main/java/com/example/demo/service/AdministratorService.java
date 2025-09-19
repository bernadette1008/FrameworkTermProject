package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Service
@Transactional
public class AdministratorService {

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    // 승인 대기중인 학생 목록 조회
    public List<Student> getPendingStudents() {
        return studentRepository.findAll().stream()
                .filter(student -> !student.isAllowed())
                .toList();
    }

    // 승인 대기중인 교수 목록 조회
    public List<Professor> getPendingProfessors() {
        return professorRepository.findAll().stream()
                .filter(professor -> !professor.isAllowed())
                .toList();
    }

    // 승인된 학생 목록 조회
    public List<Student> getApprovedStudents() {
        return studentRepository.findAll().stream()
                .filter(Student::isAllowed)
                .toList();
    }

    // 승인된 교수 목록 조회
    public List<Professor> getApprovedProfessors() {
        return professorRepository.findAll().stream()
                .filter(Professor::isAllowed)
                .toList();
    }

    // 학생 승인
    public boolean approveStudent(String studentId) {
        try {
            Student student = studentRepository.findByStudentId(studentId);
            if (student != null && !student.isAllowed()) {
                student.setAllowed(true);
                studentRepository.save(student);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error approving student: " + e.getMessage());
            return false;
        }
    }

    // 교수 승인
    public boolean approveProfessor(String professorId) {
        try {
            Professor professor = professorRepository.findByProfessorId(professorId);
            if (professor != null && !professor.isAllowed()) {
                professor.setAllowed(true);
                professorRepository.save(professor);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error approving professor: " + e.getMessage());
            return false;
        }
    }

    // 학생 승인 취소 (이미 승인된 학생의 권한 회수)
    public boolean revokeStudent(String studentId) {
        try {
            Student student = studentRepository.findByStudentId(studentId);
            if (student != null && student.isAllowed()) {
                student.setAllowed(false);
                studentRepository.save(student);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error revoking student: " + e.getMessage());
            return false;
        }
    }

    // 교수 승인 취소 (이미 승인된 교수의 권한 회수)
    public boolean revokeProfessor(String professorId) {
        try {
            Professor professor = professorRepository.findByProfessorId(professorId);
            if (professor != null && professor.isAllowed()) {
                professor.setAllowed(false);
                professorRepository.save(professor);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error revoking professor: " + e.getMessage());
            return false;
        }
    }

    // 학생 삭제 (회원가입 거부)
    public boolean rejectStudent(String studentId) {
        try {
            Student student = studentRepository.findByStudentId(studentId);
            if (student != null && !student.isAllowed()) {
                studentRepository.delete(student);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error rejecting student: " + e.getMessage());
            return false;
        }
    }

    // 교수 삭제 (회원가입 거부)
    public boolean rejectProfessor(String professorId) {
        try {
            Professor professor = professorRepository.findByProfessorId(professorId);
            if (professor != null && !professor.isAllowed()) {
                professorRepository.delete(professor);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error rejecting professor: " + e.getMessage());
            return false;
        }
    }

    // 전체 통계 조회
    public AdminStatistics getStatistics() {
        List<Student> allStudents = studentRepository.findAll();
        List<Professor> allProfessors = professorRepository.findAll();

        int pendingStudents = (int) allStudents.stream().filter(s -> !s.isAllowed()).count();
        int approvedStudents = (int) allStudents.stream().filter(Student::isAllowed).count();
        int pendingProfessors = (int) allProfessors.stream().filter(p -> !p.isAllowed()).count();
        int approvedProfessors = (int) allProfessors.stream().filter(Professor::isAllowed).count();

        return new AdminStatistics(pendingStudents, approvedStudents, pendingProfessors, approvedProfessors);
    }

    // 통계 데이터를 담을 내부 클래스
    @Getter
    public static class AdminStatistics {
        private final int pendingStudents;
        private final int approvedStudents;
        private final int pendingProfessors;
        private final int approvedProfessors;

        public AdminStatistics(int pendingStudents, int approvedStudents,
                               int pendingProfessors, int approvedProfessors) {
            this.pendingStudents = pendingStudents;
            this.approvedStudents = approvedStudents;
            this.pendingProfessors = pendingProfessors;
            this.approvedProfessors = approvedProfessors;
        }

    }
}