package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // 학생 메인 페이지 (기존 /student-main을 여기로 이동)
    @GetMapping("/main")
    public String studentMain(Model model, HttpSession session) {
        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/";
        }

        // 세션에 userId 저장 (API에서 사용)
        session.setAttribute("userId", student.getStudentId());

        // 학생의 수강 과목과 과제 정보 조회
        try {
            List<Course> courses = studentService.getStudentCourses(student.getStudentId());
            List<Assignment> assignments = studentService.getStudentAssignments(student.getStudentId());

            // 과제별 제출 상태 설정
            for (Assignment assignment : assignments) {
                boolean isSubmitted = studentService.getSubmission(
                        String.valueOf(assignment.getAssignmentCode()),
                        student.getStudentId()
                ).isPresent();

                assignment.setSubmitted(isSubmitted);
                assignment.setOverdue(assignment.getDueDate() != null &&
                        assignment.getDueDate().isBefore(LocalDateTime.now()));
            }

            model.addAttribute("student", student);
            model.addAttribute("courses", courses);
            model.addAttribute("assignments", assignments);
        } catch (Exception e) {
            model.addAttribute("error", "데이터를 불러오는데 실패했습니다.");
        }

        return "student-main";
    }

    // 과제 상세 페이지
    @GetMapping("/assignment/{assignmentCode}")
    public String assignmentDetail(@PathVariable String assignmentCode,
                                   Model model,
                                   HttpSession session) {
        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/";
        }

        try {
            // 과제 정보 조회
            Assignment assignment = studentService.getAssignmentDetails(assignmentCode);

            // 학생이 이 과제에 접근할 권한이 있는지 확인
            List<Course> studentCourses = studentService.getStudentCourses(student.getStudentId());
            boolean hasAccess = studentCourses.stream()
                    .anyMatch(course -> course.getCourseCode().equals(assignment.getCourseCode()));

            if (!hasAccess) {
                model.addAttribute("error", "접근 권한이 없는 과제입니다.");
                return "error";
            }

            model.addAttribute("student", student);
            model.addAttribute("assignment", assignment);

        } catch (Exception e) {
            model.addAttribute("error", "과제를 찾을 수 없습니다.");
            return "error";
        }

        return "student-assignment-detail";
    }

    // 수강 중인 과목 목록 페이지
    @GetMapping("/courses")
    public String studentCourses(Model model, HttpSession session) {
        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/";
        }

        try {
            List<Course> courses = studentService.getStudentCourses(student.getStudentId());
            model.addAttribute("student", student);
            model.addAttribute("courses", courses);
        } catch (Exception e) {
            model.addAttribute("error", "수강 과목을 불러오는데 실패했습니다.");
        }

        return "student-courses";
    }

    // 전체 과제 목록 페이지
    @GetMapping("/assignments")
    public String studentAssignments(Model model, HttpSession session) {
        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/";
        }

        try {
            List<Assignment> assignments = studentService.getStudentAssignments(student.getStudentId());

            // 과제별 제출 상태 설정
            for (Assignment assignment : assignments) {
                boolean isSubmitted = studentService.getSubmission(
                        String.valueOf(assignment.getAssignmentCode()),
                        student.getStudentId()
                ).isPresent();

                assignment.setSubmitted(isSubmitted);
                assignment.setOverdue(assignment.getDueDate() != null &&
                        assignment.getDueDate().isBefore(LocalDateTime.now()));
            }

            model.addAttribute("student", student);
            model.addAttribute("assignments", assignments);
        } catch (Exception e) {
            model.addAttribute("error", "과제 목록을 불러오는데 실패했습니다.");
        }

        return "student-assignments";
    }

    // 제출 현황 페이지
    @GetMapping("/submissions")
    public String studentSubmissions(Model model, HttpSession session) {
        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/";
        }

        try {
            List<Submission> submissions = studentService.getStudentSubmissions(student.getStudentId());
            model.addAttribute("student", student);
            model.addAttribute("submissions", submissions);
        } catch (Exception e) {
            model.addAttribute("error", "제출 현황을 불러오는데 실패했습니다.");
        }

        return "student-submissions";
    }

    // 내 질문 목록 페이지
    @GetMapping("/questions")
    public String studentQuestions(Model model, HttpSession session) {
        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/";
        }

        try {
            List<Question> questions = studentService.getStudentQuestions(student.getStudentId());
            model.addAttribute("student", student);
            model.addAttribute("questions", questions);
        } catch (Exception e) {
            model.addAttribute("error", "질문 목록을 불러오는데 실패했습니다.");
        }

        return "student-questions";
    }

    // 수업 등록 처리 (메인 페이지에서)
    @PostMapping("/enroll")
    public String enrollInCourse(@RequestParam String courseCode,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/";
        }

        try {
            boolean success = studentService.enrollInCourse(student.getStudentId(), courseCode.trim());

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "수업이 성공적으로 등록되었습니다.");
                return "redirect:/student/main?enrolled=success";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "유효하지 않은 수업 코드입니다.");
                return "redirect:/student/main?enrolled=error";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/main?enrolled=error";
        }
    }

    // 수강취소 처리
    @PostMapping("/unenroll")
    public String unenrollFromCourse(@RequestParam String courseCode,
                                     HttpSession session,
                                     Model model) {
        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/";
        }

        try {
            studentService.unenrollFromCourse(student.getStudentId(), courseCode);
            model.addAttribute("successMessage", "수업 등록이 해제되었습니다.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student/main?unenrolled=success";
    }
}