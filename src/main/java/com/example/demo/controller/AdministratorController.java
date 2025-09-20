package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import com.example.demo.service.AdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdministratorController {

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AdministratorService administratorService;

    // 관리자 세션 체크 메서드
    private boolean checkAdminSession(HttpSession session) {
        Administrator administrator = (Administrator) session.getAttribute("user");
        return administrator != null && "관리자".equals(session.getAttribute("userType"));
    }

    // === 관리자 메인 대시보드 ===

    @GetMapping("/main")
    public String administratorMain(Model model, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        Administrator administrator = (Administrator) session.getAttribute("user");
        AdministratorService.AdminStatistics stats = administratorService.getStatistics();

        model.addAttribute("administrator", administrator);
        model.addAttribute("stats", stats);

        return "administrator/administrator-main";
    }

    // === 사용자 승인 관리 ===

    // 승인 대기 목록 페이지
    @GetMapping("/pending-users")
    public String pendingUsers(Model model, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        List<Student> pendingStudents = administratorService.getPendingStudents();
        List<Professor> pendingProfessors = administratorService.getPendingProfessors();

        model.addAttribute("pendingStudents", pendingStudents);
        model.addAttribute("pendingProfessors", pendingProfessors);

        return "administrator/administrator-pending-users";
    }

    // 승인된 사용자 목록 페이지
    @GetMapping("/approved-users")
    public String approvedUsers(Model model, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        List<Student> approvedStudents = administratorService.getApprovedStudents();
        List<Professor> approvedProfessors = administratorService.getApprovedProfessors();

        model.addAttribute("approvedStudents", approvedStudents);
        model.addAttribute("approvedProfessors", approvedProfessors);

        return "administrator/administrator-approved-users";
    }

    // 학생 승인 처리
    @PostMapping("/approve-student")
    public String approveStudent(@RequestParam String studentId, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        administratorService.approveStudent(studentId);
        return "redirect:/admin/pending-users?approved=student";
    }

    // 교수 승인 처리
    @PostMapping("/approve-professor")
    public String approveProfessor(@RequestParam String professorId, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        administratorService.approveProfessor(professorId);
        return "redirect:/admin/pending-users?approved=professor";
    }

    // 학생 거부 처리
    @PostMapping("/reject-student")
    public String rejectStudent(@RequestParam String studentId, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        administratorService.rejectStudent(studentId);
        return "redirect:/admin/pending-users?rejected=student";
    }

    // 교수 거부 처리
    @PostMapping("/reject-professor")
    public String rejectProfessor(@RequestParam String professorId, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        administratorService.rejectProfessor(professorId);
        return "redirect:/admin/pending-users?rejected=professor";
    }

    // 학생 권한 회수 처리
    @PostMapping("/revoke-student")
    public String revokeStudent(@RequestParam String studentId, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        administratorService.revokeStudent(studentId);
        return "redirect:/admin/approved-users?revoked=student";
    }

    // 교수 권한 회수 처리
    @PostMapping("/revoke-professor")
    public String revokeProfessor(@RequestParam String professorId, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        administratorService.revokeProfessor(professorId);
        return "redirect:/admin/approved-users?revoked=professor";
    }

    // === 강의 관리 기능 ===

    // 전체 강의 목록 페이지
    @GetMapping("/courses")
    public String manageCourses(Model model, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        List<Course> courses = administratorService.getAllCourses();
        model.addAttribute("courses", courses);

        return "administrator/administrator-manage-courses";
    }

    // 강의 상세 정보 페이지
    @GetMapping("/course/{courseCode}")
    public String courseDetails(@PathVariable String courseCode, Model model, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        Course course = administratorService.getCourseDetails(courseCode);
        if (course == null) {
            return "redirect:/admin/courses?error=courseNotFound";
        }

        // 수강생 목록 조회
        List<Enrollment> enrollments = enrollmentRepository.findByCourseCode(courseCode);

        // 과제 목록 조회
        List<Assignment> assignments = assignmentRepository.findByCourseCode(courseCode);

        model.addAttribute("course", course);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("assignments", assignments);

        return "administrator/administrator-course-details";
    }

    // 강의 삭제 처리
    @PostMapping("/delete-course")
    public String deleteCourse(@RequestParam String courseCode, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        boolean success = administratorService.deleteCourse(courseCode);
        if (success) {
            return "redirect:/admin/courses?deleted=success";
        } else {
            return "redirect:/admin/courses?deleted=error";
        }
    }

    // === 과제 관리 기능 ===

    // 전체 과제 목록 페이지
    @GetMapping("/assignments")
    public String manageAssignments(Model model, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        List<Assignment> assignments = administratorService.getAllAssignments();
        model.addAttribute("assignments", assignments);

        return "administrator/administrator-manage-assignments";
    }

    // 과제 상세 정보 페이지
    @GetMapping("/assignment/{assignmentCode}")
    public String assignmentDetails(@PathVariable int assignmentCode, Model model, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        Assignment assignment = assignmentRepository.findById(assignmentCode).orElse(null);
        if (assignment == null) {
            return "redirect:/admin/assignments?error=assignmentNotFound";
        }

        // 과제 통계 조회
        AdministratorService.AssignmentStatistics stats = administratorService.getAssignmentStatistics(assignmentCode);

        // 제출물 목록 조회
        List<Submission> submissions = submissionRepository.findByAssignmentCode(assignmentCode);

        model.addAttribute("assignment", assignment);
        model.addAttribute("stats", stats);
        model.addAttribute("submissions", submissions);

        return "administrator/administrator-assignment-details";
    }

    // 과제 삭제 처리
    @PostMapping("/delete-assignment")
    public String deleteAssignment(@RequestParam int assignmentCode, HttpSession session) {
        if (!checkAdminSession(session)) {
            return "redirect:/";
        }

        boolean success = administratorService.deleteAssignment(assignmentCode);
        if (success) {
            return "redirect:/admin/assignments?deleted=success";
        } else {
            return "redirect:/admin/assignments?deleted=error";
        }
    }
}