package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/student")
public class StudentApiController {

    @Autowired
    private StudentService studentService;

    // 과제 상세 정보 조회
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getAssignmentDetails(@PathVariable String assignmentId, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            Assignment assignment = studentService.getAssignmentDetails(assignmentId);
            Optional<Submission> submission = studentService.getSubmission(assignmentId, studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("assignmentId", assignment.getAssignmentCode());
            response.put("title", assignment.getTitle());
            response.put("content", assignment.getContent());
            response.put("dueDate", assignment.getDueDate());
            response.put("createdDate", assignment.getCreatedDate());

            if (submission.isPresent()) {
                Map<String, Object> submissionData = new HashMap<>();
                submissionData.put("submissionId", submission.get().getSubmissionCode());
                submissionData.put("content", submission.get().getContent());
                submissionData.put("submittedDate", submission.get().getSubmissionTime());
                submissionData.put("lastModifiedDate", submission.get().getLastModifiedDate());
                response.put("submission", submissionData);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 과제의 질문 목록 조회
    @GetMapping("/assignment/{assignmentId}/questions")
    public ResponseEntity<?> getAssignmentQuestions(@PathVariable String assignmentId, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            List<Question> questions = studentService.getAssignmentQuestions(assignmentId, studentId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 과제 제출
    @PostMapping("/submission")
    public ResponseEntity<?> submitAssignment(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            String assignmentId = request.get("assignmentId").toString();
            String content = request.get("content").toString();

            Submission submission = studentService.submitAssignment(assignmentId, studentId, content);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "과제가 성공적으로 제출되었습니다.");
            response.put("submissionId", submission.getSubmissionCode());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 제출물 수정
    @PutMapping("/submission")
    public ResponseEntity<?> updateSubmission(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            String submissionId = request.get("submissionId").toString();
            String content = request.get("content").toString();

            studentService.updateSubmission(submissionId, content);

            return ResponseEntity.ok(createSuccessResponse("과제가 성공적으로 수정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 제출물 삭제
    @DeleteMapping("/submission/{submissionId}")
    public ResponseEntity<?> deleteSubmission(@PathVariable String submissionId, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            studentService.deleteSubmission(submissionId);

            return ResponseEntity.ok(createSuccessResponse("제출물이 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 질문 등록
    @PostMapping("/question")
    public ResponseEntity<?> submitQuestion(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            String assignmentId = request.get("assignmentId").toString();
            String content = request.get("content").toString();

            Question question = studentService.submitQuestion(assignmentId, studentId, content);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "질문이 성공적으로 등록되었습니다.");
            response.put("questionId", question.getQuestionCode());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 수업 등록
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollInCourse(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            String courseCode = request.get("courseKey");
            boolean success = studentService.enrollInCourse(studentId, courseCode);

            if (success) {
                return ResponseEntity.ok(createSuccessResponse("수업이 성공적으로 등록되었습니다."));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("유효하지 않은 수업 코드입니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Helper methods
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}