package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.dto.SubmissionDTO;
import com.example.demo.service.ProfessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

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

    // ProfessorController.java에 추가할 메서드들

    // 강의 관리 메인 페이지
    @GetMapping("/courses")
    public String courseManagement(Model model, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            List<Course> courses = professorService.getProfessorCourses(professor.getProfessorId());
            // 각 강의의 수강생 수와 과제 수도 함께 조회
            for (Course course : courses) {
                List<Student> students = professorService.getCourseStudents(course.getCourseCode());
                List<Assignment> assignments = professorService.getCourseAssignments(course.getCourseCode());
                // Transient 필드 활용 (Course 엔티티에 추가 필요)
                course.setStudentCount(students.size());
                course.setAssignmentCount(assignments.size());
            }

            model.addAttribute("professor", professor);
            model.addAttribute("courses", courses);
        } catch (Exception e) {
            model.addAttribute("error", "강의 목록을 불러오는데 실패했습니다.");
        }

        return "professor/professor-course-management";
    }

    // 강의 상세 관리 페이지
    @GetMapping("/course/{courseCode}/manage")
    public String courseDetail(@PathVariable String courseCode,
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
            List<Assignment> assignments = professorService.getCourseAssignments(courseCode);

            model.addAttribute("professor", professor);
            model.addAttribute("course", course);
            model.addAttribute("students", students);
            model.addAttribute("assignments", assignments);
        } catch (Exception e) {
            model.addAttribute("error", "강의 정보를 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-course-detail";
    }

    // 강의 삭제
    @PostMapping("/course/{courseCode}/delete")
    public String deleteCourse(@PathVariable String courseCode,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Course course = professorService.getCourseDetails(courseCode);

            // 교수 권한 확인
            if (!course.getProfessorId().equals(professor.getProfessorId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "권한이 없습니다.");
                return "redirect:/professor/courses";
            }

            professorService.deleteCourse(courseCode);
            redirectAttributes.addFlashAttribute("successMessage", "강의가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/professor/courses";
    }

    // 수강생 강제 탈퇴
    @PostMapping("/course/{courseCode}/remove-student/{studentId}")
    public String removeStudent(@PathVariable String courseCode,
                                @PathVariable String studentId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Course course = professorService.getCourseDetails(courseCode);

            // 교수 권한 확인
            if (!course.getProfessorId().equals(professor.getProfessorId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "권한이 없습니다.");
                return "redirect:/professor/course/" + courseCode + "/manage";
            }

            professorService.removeStudentFromCourse(studentId, courseCode);
            redirectAttributes.addFlashAttribute("successMessage", "수강생을 제외했습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/professor/course/" + courseCode + "/manage";
    }

    // 학생 성적 조회 API (AJAX용)
    @GetMapping("/course/{courseCode}/student/{studentId}/grades")
    @ResponseBody
    public List<SubmissionDTO> getStudentGrades(@PathVariable String courseCode,
                                                @PathVariable String studentId,
                                                HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        try {
            Course course = professorService.getCourseDetails(courseCode);

            // 교수 권한 확인
            if (!course.getProfessorId().equals(professor.getProfessorId())) {
                throw new RuntimeException("접근 권한이 없습니다.");
            }

            List<Submission> submissions = professorService.getStudentSubmissionsInCourse(studentId, courseCode);

            // DTO로 변환하여 반환
            return submissions.stream()
                    .map(SubmissionDTO::new)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("성적 정보를 불러올 수 없습니다: " + e.getMessage());
        }
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