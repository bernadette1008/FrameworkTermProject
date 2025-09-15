package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
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
    @GetMapping("/assignment/{assignmentCode}")
    public ResponseEntity<?> getAssignmentDetails(@PathVariable int assignmentCode, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            Assignment assignment = studentService.getAssignmentDetails(assignmentCode);
            Optional<Submission> submission = studentService.getSubmission(assignmentCode, studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("assignmentId", assignment.getAssignmentCode()); // JavaScript 호환성을 위해
            response.put("assignmentCode", assignment.getAssignmentCode());
            response.put("title", assignment.getTitle());
            response.put("content", assignment.getContent());
            response.put("dueDate", assignment.getDueDate());
            response.put("createdDate", assignment.getCreatedDate());

            if (submission.isPresent()) {
                Map<String, Object> submissionData = new HashMap<>();
                submissionData.put("submissionId", submission.get().getSubmissionCode());
                submissionData.put("submissionCode", submission.get().getSubmissionCode());
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
    @GetMapping("/assignment/{assignmentCode}/questions")
    public ResponseEntity<?> getAssignmentQuestions(@PathVariable int assignmentCode, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            List<Question> questions = studentService.getAssignmentQuestions(assignmentCode, studentId);

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

            // 안전한 타입 변환
            Object assignmentCodeObj = request.get("assignmentId") != null ?
                    request.get("assignmentId") : request.get("assignmentCode");
            Object contentObj = request.get("content");

            if (assignmentCodeObj == null || contentObj == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("필수 정보가 누락되었습니다."));
            }

            // Object를 안전하게 int로 변환
            int assignmentCode = parseToInt(assignmentCodeObj);
            String content = contentObj.toString();

            if (content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("제출 내용을 입력해주세요."));
            }

            Submission submission = studentService.submitAssignment(assignmentCode, studentId, content);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "과제가 성공적으로 제출되었습니다.");
            response.put("submissionId", submission.getSubmissionCode());
            response.put("submissionCode", submission.getSubmissionCode());

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("유효하지 않은 과제 ID입니다."));
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

            // 안전한 타입 변환
            Object submissionCodeObj = request.get("submissionId") != null ?
                    request.get("submissionId") : request.get("submissionCode");
            Object contentObj = request.get("content");

            if (submissionCodeObj == null || contentObj == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("필수 정보가 누락되었습니다."));
            }

            // Object를 안전하게 int로 변환
            int submissionCode = parseToInt(submissionCodeObj);
            String content = contentObj.toString();

            if (content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("수정할 내용을 입력해주세요."));
            }

            studentService.updateSubmission(submissionCode, content);

            return ResponseEntity.ok(createSuccessResponse("과제가 성공적으로 수정되었습니다."));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("유효하지 않은 제출물 ID입니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 제출물 삭제
    @DeleteMapping("/submission/{submissionCode}")
    public ResponseEntity<?> deleteSubmission(@PathVariable int submissionCode, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            studentService.deleteSubmission(submissionCode);

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

            // 안전한 타입 변환
            Object assignmentCodeObj = request.get("assignmentId") != null ?
                    request.get("assignmentId") : request.get("assignmentCode");
            Object contentObj = request.get("content");

            if (assignmentCodeObj == null || contentObj == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("필수 정보가 누락되었습니다."));
            }

            // Object를 안전하게 int로 변환
            int assignmentCode = parseToInt(assignmentCodeObj);
            String content = contentObj.toString();

            if (content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("질문 내용을 입력해주세요."));
            }

            Question question = studentService.submitQuestion(assignmentCode, studentId, content);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "질문이 성공적으로 등록되었습니다.");
            response.put("questionId", question.getQuestionCode());
            response.put("questionCode", question.getQuestionCode());

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("유효하지 않은 과제 ID입니다."));
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
            if (courseCode == null || courseCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("수업 코드를 입력해주세요."));
            }

            boolean success = studentService.enrollInCourse(studentId, courseCode.trim());

            if (success) {
                return ResponseEntity.ok(createSuccessResponse("수업이 성공적으로 등록되었습니다."));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("유효하지 않은 수업 코드입니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 수업 등록 해제
    @DeleteMapping("/enroll/{courseCode}")
    public ResponseEntity<?> unenrollFromCourse(@PathVariable String courseCode, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            studentService.unenrollFromCourse(studentId, courseCode);

            return ResponseEntity.ok(createSuccessResponse("수업 등록이 해제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 학생의 모든 제출물 조회
    @GetMapping("/submissions")
    public ResponseEntity<?> getStudentSubmissions(HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            List<Submission> submissions = studentService.getStudentSubmissions(studentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 학생의 모든 질문 조회
    @GetMapping("/questions")
    public ResponseEntity<?> getStudentQuestions(HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            List<Question> questions = studentService.getStudentQuestions(studentId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Object를 안전하게 int로 변환하는 유틸리티 메서드
    private int parseToInt(Object obj) throws NumberFormatException {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof String) {
            return Integer.parseInt((String) obj);
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else {
            throw new NumberFormatException("Cannot convert " + obj.getClass().getSimpleName() + " to int");
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