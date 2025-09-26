package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.dto.QuestionDTO;
import com.example.demo.dto.SubmissionDTO;
import com.example.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.File;

@RestController
@RequestMapping("/api/student")
public class StudentApiController {

    @Autowired
    private StudentService studentService;

    // 과제 상세 정보 조회 (수정된 버전)
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
            response.put("assignmentId", assignment.getAssignmentCode());
            response.put("assignmentCode", assignment.getAssignmentCode());
            response.put("title", assignment.getTitle());
            response.put("content", assignment.getContent());
            response.put("dueDate", assignment.getDueDate());
            response.put("createdDate", assignment.getCreatedDate());

            if (submission.isPresent()) {
                Submission sub = submission.get();
                Map<String, Object> submissionData = new HashMap<>();
                submissionData.put("submissionId", sub.getSubmissionCode());
                submissionData.put("submissionCode", sub.getSubmissionCode());
                submissionData.put("content", sub.getContent());
                submissionData.put("submittedDate", sub.getSubmissionTime());
                submissionData.put("lastModifiedDate", sub.getLastModifiedDate());
                submissionData.put("score", sub.getScore());
                submissionData.put("feedback", sub.getFeedback());

                // 파일 정보 추가
                submissionData.put("hasFile", sub.getFileName() != null && !sub.getFileName().isEmpty());
                if (sub.getFileName() != null && !sub.getFileName().isEmpty()) {
                    submissionData.put("fileName", sub.getOriginalFileName() != null ? sub.getOriginalFileName() : sub.getFileName());
                    submissionData.put("fileSize", sub.getFileSize());
                    submissionData.put("fileContentType", sub.getFileContentType());
                }

                response.put("submission", submissionData);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 과제의 질문 목록 조회 - DTO 사용
    @GetMapping("/assignment/{assignmentCode}/questions")
    public ResponseEntity<?> getAssignmentQuestions(@PathVariable int assignmentCode, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            System.out.println("DEBUG: Loading questions for studentId: " + studentId + ", assignmentCode: " + assignmentCode);

            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            List<QuestionDTO> questions = studentService.getAssignmentQuestionsDTO(assignmentCode, studentId);
            System.out.println("DEBUG: Found " + questions.size() + " questions");

            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            System.err.println("DEBUG: Error in getAssignmentQuestions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 과제 제출 (텍스트만)
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

    // 과제 제출 (파일 포함)
    @PostMapping("/submission/file")
    public ResponseEntity<?> submitAssignmentWithFile(
            @RequestParam("assignmentCode") int assignmentCode,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            if (content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("제출 내용을 입력해주세요."));
            }

            // 파일이 있으면 파일과 함께 제출, 없으면 텍스트만 제출
            Submission submission;
            if (file != null && !file.isEmpty()) {
                submission = studentService.submitAssignmentWithFile(assignmentCode, studentId, content, file);
            } else {
                submission = studentService.submitAssignment(assignmentCode, studentId, content);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "과제가 성공적으로 제출되었습니다.");
            response.put("submissionId", submission.getSubmissionCode());
            response.put("submissionCode", submission.getSubmissionCode());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/submission/{submissionCode}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable int submissionCode, HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");
            Object user = session.getAttribute("user");

            if (userId == null || user == null) {
                return ResponseEntity.status(401).build();
            }

            // 제출물 조회
            Submission submission = studentService.getSubmissionById(submissionCode);
            if (submission == null) {
                return ResponseEntity.notFound().build();
            }

            // 권한 확인: 학생 본인 또는 담당 교수만 다운로드 가능
            boolean hasPermission = false;

            if (user instanceof Student) {
                // 학생인 경우: 본인의 제출물인지 확인
                hasPermission = submission.getStudentId().equals(userId);
            } else if (user instanceof Professor) {
                // 교수인 경우: 해당 과제의 담당 교수인지 확인
                Assignment assignment = studentService.getAssignmentDetails(submission.getAssignmentCode());
                hasPermission = assignment.getCourse().getProfessorId().equals(((Professor) user).getProfessorId());
            }

            if (!hasPermission) {
                return ResponseEntity.status(403).build();
            }

            // 파일이 존재하는지 확인
            if (submission.getFilePath() == null || submission.getFilePath().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            File file = new File(submission.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            // 파일명 UTF-8 인코딩
            String encodedFileName = URLEncoder.encode(
                    submission.getOriginalFileName() != null ? submission.getOriginalFileName() : submission.getFileName(),
                    StandardCharsets.UTF_8.toString()
            ).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .header(HttpHeaders.CONTENT_TYPE, submission.getFileContentType() != null ?
                            submission.getFileContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))
                    .body(resource);

        } catch (Exception e) {
            System.err.println("Error downloading file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 제출물 수정 (텍스트만)
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

    // 제출물 수정 (파일 포함)
    @PostMapping("/submission/update")
    public ResponseEntity<?> updateSubmissionWithFile(
            @RequestParam("submissionCode") int submissionCode,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            if (content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("수정할 내용을 입력해주세요."));
            }

            // 파일이 있으면 파일과 함께 수정, 없으면 텍스트만 수정
            if (file != null && !file.isEmpty()) {
                studentService.updateSubmissionWithFile(submissionCode, content, file);
            } else {
                studentService.updateSubmission(submissionCode, content);
            }

            return ResponseEntity.ok(createSuccessResponse("과제가 성공적으로 수정되었습니다."));
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

    // 학생의 모든 제출물 조회 - DTO 사용
    @GetMapping("/submissions")
    public ResponseEntity<?> getStudentSubmissions(HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            System.out.println("DEBUG: Getting submissions for studentId: " + studentId);

            if (studentId == null) {
                System.out.println("DEBUG: No studentId in session");
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            List<SubmissionDTO> submissions = studentService.getStudentSubmissionsDTO(studentId);
            System.out.println("DEBUG: Retrieved " + submissions.size() + " submissions");

            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            System.err.println("DEBUG: Error in getStudentSubmissions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 학생의 모든 질문 조회
    @GetMapping("/questions")
    public ResponseEntity<?> getStudentQuestions(HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("userId");
            System.out.println("DEBUG: Getting questions for studentId: " + studentId);

            if (studentId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("로그인이 필요합니다."));
            }

            List<QuestionDTO> questions = studentService.getStudentQuestionsDTO(studentId);
            System.out.println("DEBUG: Retrieved " + questions.size() + " questions");

            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            System.err.println("DEBUG: Error in getStudentQuestions: " + e.getMessage());
            e.printStackTrace();
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