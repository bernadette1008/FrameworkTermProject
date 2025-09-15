package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.service.ProfessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/professor")
public class ProfessorController {

    @Autowired
    private ProfessorService professorService;

    // 과제 관리 메인 페이지
    @GetMapping("/assignments")
    public String assignmentManagement(Model model, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            List<Assignment> assignments = professorService.getProfessorAssignments(professor.getProfessorId());
            model.addAttribute("professor", professor);
            model.addAttribute("assignments", assignments);
        } catch (Exception e) {
            model.addAttribute("error", "과제 목록을 불러오는데 실패했습니다.");
        }

        return "professor/professor-assignments";
    }

    // 특정 과제의 제출물 목록 조회
    @GetMapping("/assignment/{assignmentCode}/submissions")
    public String assignmentSubmissions(@PathVariable int assignmentCode,
                                        Model model,
                                        HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Assignment assignment = professorService.getAssignmentDetails(assignmentCode);
            List<Submission> submissions = professorService.getAssignmentSubmissions(assignmentCode);

            // 교수 권한 확인
            if (!assignment.getCourse().getProfessorId().equals(professor.getProfessorId())) {
                model.addAttribute("error", "접근 권한이 없습니다.");
                return "error";
            }

            model.addAttribute("professor", professor);
            model.addAttribute("assignment", assignment);
            model.addAttribute("submissions", submissions);
        } catch (Exception e) {
            model.addAttribute("error", "과제 정보를 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-submissions";
    }

    // 제출물 상세 보기 및 채점
    @GetMapping("/submission/{submissionCode}")
    public String submissionDetail(@PathVariable int submissionCode,
                                   Model model,
                                   HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Submission submission = professorService.getSubmissionDetails(submissionCode);

            // 교수 권한 확인
            if (!submission.getAssignment().getCourse().getProfessorId().equals(professor.getProfessorId())) {
                model.addAttribute("error", "접근 권한이 없습니다.");
                return "error";
            }

            model.addAttribute("professor", professor);
            model.addAttribute("submission", submission);
        } catch (Exception e) {
            model.addAttribute("error", "제출물을 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-submission-detail";
    }

    // 채점 처리
    @PostMapping("/submission/{submissionCode}/grade")
    public String gradeSubmission(@PathVariable int submissionCode,
                                  @RequestParam Integer score,
                                  @RequestParam(required = false) String feedback,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            professorService.gradeSubmission(submissionCode, score, feedback);
            redirectAttributes.addFlashAttribute("successMessage", "채점이 완료되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/professor/submission/" + submissionCode;
    }

    // 질문 관리 페이지
    @GetMapping("/questions")
    public String questionManagement(Model model, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            List<Question> questions = professorService.getProfessorQuestions(professor.getProfessorId());
            model.addAttribute("professor", professor);
            model.addAttribute("questions", questions);
        } catch (Exception e) {
            model.addAttribute("error", "질문 목록을 불러오는데 실패했습니다.");
        }

        return "professor/professor-question-management";
    }

    // 질문 상세 보기
    @GetMapping("/question/{questionCode}")
    public String questionDetail(@PathVariable int questionCode,
                                 Model model,
                                 HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Question question = professorService.getQuestionDetails(questionCode);

            // 교수 권한 확인
            if (!question.getAssignment().getCourse().getProfessorId().equals(professor.getProfessorId())) {
                model.addAttribute("error", "접근 권한이 없습니다.");
                return "error";
            }

            model.addAttribute("professor", professor);
            model.addAttribute("question", question);
        } catch (Exception e) {
            model.addAttribute("error", "질문을 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-question-detail";
    }

    // 질문 답변 처리
    @PostMapping("/question/{questionCode}/answer")
    public String answerQuestion(@PathVariable int questionCode,
                                 @RequestParam String content,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            professorService.answerQuestion(questionCode, professor.getProfessorId(), content);
            redirectAttributes.addFlashAttribute("successMessage", "답변이 등록되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/professor/question/" + questionCode;
    }

    // 수강생 관리 페이지
    @GetMapping("/students")
    public String studentManagement(Model model, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            List<Course> courses = professorService.getProfessorCourses(professor.getProfessorId());
            model.addAttribute("professor", professor);
            model.addAttribute("courses", courses);
        } catch (Exception e) {
            model.addAttribute("error", "강의 목록을 불러오는데 실패했습니다.");
        }

        return "professor/professor-student-management";
    }

    // 특정 강의의 수강생 목록
    @GetMapping("/course/{courseCode}/students")
    public String courseStudents(@PathVariable String courseCode,
                                 Model model,
                                 HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Course course = professorService.getCourseDetails(courseCode);

            // 교수 권한 확인
            if (!course.getProfessorId().equals(professor.getProfessorId())) {
                model.addAttribute("error", "접근 권한이 없습니다.");
                return "error";
            }

            List<Student> students = professorService.getCourseStudents(courseCode);

            model.addAttribute("professor", professor);
            model.addAttribute("course", course);
            model.addAttribute("students", students);
        } catch (Exception e) {
            model.addAttribute("error", "수강생 목록을 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-course-students";
    }
}