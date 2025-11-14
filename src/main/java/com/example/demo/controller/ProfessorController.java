package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.dto.SubmissionDTO;
import com.example.demo.service.ProfessorService;
import com.example.demo.util.XSSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/professor")
public class ProfessorController {

    @Autowired
    private ProfessorService professorService;

    private boolean hasPermissionForCourse(Professor professor, Course course) {
        if (course == null || professor == null) {
            return false;
        }

        // 메인 교수인 경우
        if (course.getProfessorId().equals(professor.getProfessorId())) {
            return true;
        }

        // 부교수인 경우
        if (course.getSubProfessors() != null &&
                course.getSubProfessors().contains(professor.getProfessorId())) {
            return true;
        }

        return false;
    }

    private boolean isMainProfessor(Professor professor, Course course) {
        if (course == null || professor == null) {
            return false;
        }
        return course.getProfessorId().equals(professor.getProfessorId());
    }

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

    // 강의 코드로 부교수 추가
    @PostMapping("/join-course")
    @ResponseBody  // JSON 응답을 위해 추가
    public ResponseEntity<Map<String, Object>> joinCourse(@RequestParam String courseCode,
                                                          HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (professor == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            // XSS 검증
            XSSUtils.validateInput(courseCode, "강의 코드");

            professorService.addSubProfessorToCourse(courseCode.trim().toUpperCase(), professor.getProfessorId());

            response.put("success", true);
            response.put("message", "강의에 부교수로 추가되었습니다.");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "강의 추가 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 특정 과제의 질문 목록 조회
    @GetMapping("/assignment/{assignmentCode}/questions")
    public String assignmentQuestions(@PathVariable int assignmentCode,
                                      Model model,
                                      HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Assignment assignment = professorService.getAssignmentDetails(assignmentCode);
            List<Question> questions = professorService.getAssignmentQuestions(assignmentCode);

            // 교수 권한 확인
//            if (!assignment.getCourse().getProfessorId().equals(professor.getProfessorId())) {
//                model.addAttribute("error", "접근 권한이 없습니다.");
//                return "error";
//            }

            model.addAttribute("professor", professor);
            model.addAttribute("assignment", assignment);
            model.addAttribute("questions", questions);
        } catch (Exception e) {
            model.addAttribute("error", "질문 목록을 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-assignment-questions";
    }

    // 강의 상세 정보 페이지 (수정)
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

            // 권한 확인 (메인 교수 또는 부교수)
//            if (!hasPermissionForCourse(professor, course)) {
//                model.addAttribute("error", "접근 권한이 없습니다.");
//                return "error";
//            }

            List<Student> students = professorService.getCourseStudents(courseCode);
            List<Assignment> assignments = professorService.getCourseAssignments(courseCode);

            model.addAttribute("professor", professor);
            model.addAttribute("course", course);
            model.addAttribute("students", students);
            model.addAttribute("assignments", assignments);
            model.addAttribute("isMainProfessor", isMainProfessor(professor, course)); // 추가

        } catch (Exception e) {
            model.addAttribute("error", "강의 정보를 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-course-detail";
    }

    // 강의 삭제 (수정 - 메인 교수만 가능)
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

            // 메인 교수만 삭제 가능
            if (!isMainProfessor(professor, course)) {
                redirectAttributes.addFlashAttribute("errorMessage", "강의 삭제는 메인 교수만 가능합니다.");
                return "redirect:/professor/courses";
            }

            professorService.deleteCourse(courseCode);
            redirectAttributes.addFlashAttribute("successMessage", "강의가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/professor/courses";
    }

    // 부교수로 등록된 강의에서 나가기
    @PostMapping("/leave-course/{courseCode}")
    public String leaveCourse(@PathVariable String courseCode,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Course course = professorService.getCourseDetails(courseCode);

            // 메인 교수는 나갈 수 없음
            if (course.getProfessorId().equals(professor.getProfessorId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "메인 교수는 강의에서 나갈 수 없습니다. 강의를 삭제해주세요.");
                return "redirect:/professor/courses";
            }

            professorService.removeSubProfessorFromCourse(courseCode, professor.getProfessorId());
            redirectAttributes.addFlashAttribute("successMessage", "강의에서 나갔습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/professor/courses";
    }

    // 과제 삭제 (수정)
    @PostMapping("/assignment/{assignmentCode}/delete")
    public String deleteAssignment(@PathVariable int assignmentCode,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Assignment assignment = professorService.getAssignmentDetails(assignmentCode);

            // 권한 확인 (메인 교수 또는 부교수)
//            if (!hasPermissionForCourse(professor, assignment.getCourse())) {
//                redirectAttributes.addFlashAttribute("errorMessage", "권한이 없습니다.");
//                return "redirect:/professor/assignments";
//            }

            professorService.deleteAssignment(assignmentCode);
            redirectAttributes.addFlashAttribute("successMessage", "과제가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/professor/assignments";
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
//            if (!course.getProfessorId().equals(professor.getProfessorId())) {
//                redirectAttributes.addFlashAttribute("errorMessage", "권한이 없습니다.");
//                return "redirect:/professor/course/" + courseCode + "/manage";
//            }

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
//            if (!course.getProfessorId().equals(professor.getProfessorId())) {
//                throw new RuntimeException("접근 권한이 없습니다.");
//            }

            List<Submission> submissions = professorService.getStudentSubmissionsInCourse(studentId, courseCode);

            // DTO로 변환하여 반환
            return submissions.stream()
                    .map(SubmissionDTO::new)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("성적 정보를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    // 질문 관리 페이지
    @GetMapping("/questions")
    public String questionManagement(@RequestParam(required = false) String courseCode,
                                     @RequestParam(required = false) String assignmentCode,
                                     Model model,
                                     HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            // 교수의 모든 강의 목록 조회
            List<Course> courses = professorService.getProfessorCourses(professor.getProfessorId());

            // 초기 과제 목록 설정 (강의가 선택되지 않은 경우 모든 과제)
            List<Assignment> assignments = new ArrayList<>();
            if (courseCode != null && !courseCode.isEmpty()) {
                // 특정 강의의 과제만 조회
                assignments = professorService.getCourseAssignments(courseCode);
            } else {
                // 모든 강의의 과제 목록 조회 (기본값)
                assignments = professorService.getProfessorAssignments(professor.getProfessorId());
            }

            // 필터링된 질문 목록 조회
            List<Question> questions;
            if (assignmentCode != null && !assignmentCode.isEmpty()) {
                // 특정 과제의 질문만 조회
                questions = professorService.getAssignmentQuestions(Integer.parseInt(assignmentCode));
            } else if (courseCode != null && !courseCode.isEmpty()) {
                // 특정 강의의 모든 질문 조회
                questions = professorService.getCourseQuestions(courseCode);
            } else {
                // 교수의 모든 질문 조회 (기본값)
                questions = professorService.getProfessorQuestions(professor.getProfessorId());
            }

            model.addAttribute("professor", professor);
            model.addAttribute("courses", courses);
            model.addAttribute("assignments", assignments);
            model.addAttribute("questions", questions);
            model.addAttribute("selectedCourseCode", courseCode);
            model.addAttribute("selectedAssignmentCode", assignmentCode);

        } catch (Exception e) {
            model.addAttribute("error", "질문 목록을 불러오는데 실패했습니다: " + e.getMessage());
        }

        return "professor/professor-question-management";
    }

    // 강의별 과제 목록 조회 API (AJAX용)
    @GetMapping("/course/{courseCode}/assignments-json")
    @ResponseBody
    public List<Assignment> getCourseAssignmentsJson(@PathVariable String courseCode,
                                                     HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        try {
            if (courseCode.equals("all")) {
                return professorService.getProfessorAssignments(professor.getProfessorId());
            } else {
                Course course = professorService.getCourseDetails(courseCode);
                // 교수 권한 확인
//                if (!course.getProfessorId().equals(professor.getProfessorId())) {
//                    throw new RuntimeException("접근 권한이 없습니다.");
//                }
                return professorService.getCourseAssignments(courseCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("과제 목록을 불러올 수 없습니다: " + e.getMessage());
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
//            if (!assignment.getCourse().getProfessorId().equals(professor.getProfessorId())) {
//                model.addAttribute("error", "접근 권한이 없습니다.");
//                return "error";
//            }

            model.addAttribute("professor", professor);
            model.addAttribute("assignment", assignment);
            model.addAttribute("submissions", submissions);
        } catch (Exception e) {
            model.addAttribute("error", "과제 정보를 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-submissions";
    }

    // 과제 수정 페이지 (수정)
    @GetMapping("/assignment/{assignmentCode}/edit")
    public String editAssignmentForm(@PathVariable int assignmentCode,
                                     Model model,
                                     HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Assignment assignment = professorService.getAssignmentDetails(assignmentCode);

            // 권한 확인 (메인 교수 또는 부교수)
//            if (!hasPermissionForCourse(professor, assignment.getCourse())) {
//                model.addAttribute("error", "접근 권한이 없습니다.");
//                return "error";
//            }

            // 교수가 담당하는 강의 목록
            List<Course> courses = professorService.getProfessorCourses(professor.getProfessorId());

            model.addAttribute("professor", professor);
            model.addAttribute("assignment", assignment);
            model.addAttribute("courses", courses);
        } catch (Exception e) {
            model.addAttribute("error", "과제 정보를 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-assignment-edit";
    }

    // 과제 수정 처리 (수정)
    @PostMapping("/assignment/{assignmentCode}/edit")
    public String updateAssignment(@PathVariable int assignmentCode,
                                   @RequestParam String courseCode,
                                   @RequestParam String title,
                                   @RequestParam String content,
                                   @RequestParam String dueDate,
                                   @RequestParam String dueTime,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Assignment assignment = professorService.getAssignmentDetails(assignmentCode);

//            // 권한 확인 (메인 교수 또는 부교수)
//            if (!hasPermissionForCourse(professor, assignment.getCourse())) {
//                redirectAttributes.addFlashAttribute("errorMessage", "권한이 없습니다.");
//                return "redirect:/professor/assignments";
//            }
//
//            // 선택한 강의도 해당 교수가 접근 가능한지 확인
//            Course selectedCourse = professorService.getCourseDetails(courseCode);
//            if (!hasPermissionForCourse(professor, selectedCourse)) {
//                redirectAttributes.addFlashAttribute("errorMessage", "선택한 강의에 대한 권한이 없습니다.");
//                return "redirect:/professor/assignment/" + assignmentCode + "/edit";
//            }

            // 입력값 유효성 검사
            if (title == null || title.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "과제 제목을 입력해주세요.");
                return "redirect:/professor/assignment/" + assignmentCode + "/edit";
            }

            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "과제 내용을 입력해주세요.");
                return "redirect:/professor/assignment/" + assignmentCode + "/edit";
            }

            // XSS 검증
            if (XSSUtils.containsXSS(title) || XSSUtils.containsXSS(content)) {
                redirectAttributes.addFlashAttribute("errorMessage", "제목 또는 내용에 허용되지 않는 문자가 포함되어 있습니다.");
                return "redirect:/professor/assignment/" + assignmentCode + "/edit";
            }

            // 길이 제한
            if (title.length() > 200) {
                redirectAttributes.addFlashAttribute("errorMessage", "과제 제목이 너무 깁니다. (최대 200자)");
                return "redirect:/professor/assignment/" + assignmentCode + "/edit";
            }

            if (content.length() > 5000) {
                redirectAttributes.addFlashAttribute("errorMessage", "과제 내용이 너무 깁니다. (최대 5,000자)");
                return "redirect:/professor/assignment/" + assignmentCode + "/edit";
            }

            professorService.updateAssignment(assignmentCode, courseCode, title.trim(), content.trim(), dueDate, dueTime);
            redirectAttributes.addFlashAttribute("successMessage", "과제가 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            // XSS 검증 실패
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/professor/assignment/" + assignmentCode + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/professor/assignment/" + assignmentCode + "/edit";
        }

        return "redirect:/professor/assignment/" + assignmentCode + "/submissions";
    }

    // 제출물 상세 보기 및 채점 (세션 설정 추가)
    @GetMapping("/submission/{submissionCode}")
    public String submissionDetail(@PathVariable int submissionCode,
                                   Model model,
                                   HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        // API 다운로드를 위한 세션 설정
        session.setAttribute("userId", professor.getProfessorId());

        try {
            Submission submission = professorService.getSubmissionDetails(submissionCode);

            // 교수 권한 확인
//            if (!submission.getAssignment().getCourse().getProfessorId().equals(professor.getProfessorId())) {
//                model.addAttribute("error", "접근 권한이 없습니다.");
//                return "error";
//            }

            model.addAttribute("professor", professor);
            model.addAttribute("submission", submission);
        } catch (Exception e) {
            model.addAttribute("error", "제출물을 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-submission-detail";
    }

//    // 채점 처리
//    @PostMapping("/submission/{submissionCode}/grade")
//    public String gradeSubmission(@PathVariable int submissionCode,
//                                  @RequestParam Integer score,
//                                  @RequestParam(required = false) String feedback,
//                                  HttpSession session,
//                                  RedirectAttributes redirectAttributes) {
//        Professor professor = (Professor) session.getAttribute("user");
//        if (professor == null) {
//            return "redirect:/";
//        }
//
//        try {
//            professorService.gradeSubmission(submissionCode, score, feedback);
//            redirectAttributes.addFlashAttribute("successMessage", "채점이 완료되었습니다.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//        }
//
//        return "redirect:/professor/submission/" + submissionCode;
//    }

    // 제출물 채점 (피드백 XSS 방어 추가)
    @PostMapping("/submission/{submissionCode}/grade")
    public String gradeSubmission(@PathVariable int submissionCode,
                                  @RequestParam("score") int score,
                                  @RequestParam(value = "feedback", required = false) String feedback,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            Submission submission = professorService.getSubmissionDetails(submissionCode);

            // 권한 확인 (메인 교수 또는 부교수)
//            if (!hasPermissionForCourse(professor, submission.getAssignment().getCourse())) {
//                redirectAttributes.addFlashAttribute("error", "권한이 없습니다.");
//                return "redirect:/professor/assignments";
//            }

            // 피드백 XSS 검증
            if (feedback != null && !feedback.trim().isEmpty()) {
                if (XSSUtils.containsXSS(feedback)) {
                    redirectAttributes.addFlashAttribute("error", "피드백에 허용되지 않는 문자가 포함되어 있습니다.");
                    return "redirect:/professor/submission/" + submissionCode;
                }

                if (feedback.length() > 1000) {
                    redirectAttributes.addFlashAttribute("error", "피드백이 너무 깁니다. (최대 1,000자)");
                    return "redirect:/professor/submission/" + submissionCode;
                }
            }

            // 점수와 피드백 저장
            professorService.gradeSubmission(submissionCode, score, feedback);
            redirectAttributes.addFlashAttribute("successMessage", "채점이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            // XSS 검증 실패
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "채점 처리 중 오류가 발생했습니다: " + e.getMessage());
        }

        // 채점 후 다시 해당 제출물 상세 페이지로 리다이렉트
        return "redirect:/professor/submission/" + submissionCode;
    }

    // 질문 관리 페이지
//    @GetMapping("/questions")
//    public String questionManagement(Model model, HttpSession session) {
//        Professor professor = (Professor) session.getAttribute("user");
//        if (professor == null) {
//            return "redirect:/";
//        }
//
//        try {
//            List<Question> questions = professorService.getProfessorQuestions(professor.getProfessorId());
//            model.addAttribute("professor", professor);
//            model.addAttribute("questions", questions);
//        } catch (Exception e) {
//            model.addAttribute("error", "질문 목록을 불러오는데 실패했습니다.");
//        }
//
//        return "professor/professor-question-management";
//    }

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
//            if (!question.getAssignment().getCourse().getProfessorId().equals(professor.getProfessorId())) {
//                model.addAttribute("error", "접근 권한이 없습니다.");
//                return "error";
//            }

            model.addAttribute("professor", professor);
            model.addAttribute("question", question);
        } catch (Exception e) {
            model.addAttribute("error", "질문을 불러오는데 실패했습니다.");
            return "error";
        }

        return "professor/professor-question-detail";
    }

    // 질문 답변 처리 (XSS 방어 추가)
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
            Question question = professorService.getQuestionDetails(questionCode);

            // 권한 확인 (메인 교수 또는 부교수)
//            if (!hasPermissionForCourse(professor, question.getAssignment().getCourse())) {
//                redirectAttributes.addFlashAttribute("errorMessage", "권한이 없습니다.");
//                return "redirect:/professor/questions";
//            }

            // 빈 내용 체크
            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "답변 내용을 입력해주세요.");
                return "redirect:/professor/question/" + questionCode;
            }

            // XSS 검증 (이미 서비스에서 하지만 컨트롤러에서도 추가로)
            if (XSSUtils.containsXSS(content)) {
                redirectAttributes.addFlashAttribute("errorMessage", "답변 내용에 허용되지 않는 문자가 포함되어 있습니다.");
                return "redirect:/professor/question/" + questionCode;
            }

            // 길이 제한
            if (content.length() > 3000) {
                redirectAttributes.addFlashAttribute("errorMessage", "답변이 너무 깁니다. (최대 3,000자)");
                return "redirect:/professor/question/" + questionCode;
            }

            professorService.answerQuestion(questionCode, professor.getProfessorId(), content);
            redirectAttributes.addFlashAttribute("successMessage", "답변이 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            // XSS 검증 실패
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "답변 등록 중 오류가 발생했습니다: " + e.getMessage());
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
//            if (!course.getProfessorId().equals(professor.getProfessorId())) {
//                model.addAttribute("error", "접근 권한이 없습니다.");
//                return "error";
//            }

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