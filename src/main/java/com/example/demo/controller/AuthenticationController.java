package com.example.demo.controller;

import com.example.demo.domain.Administrator;
import com.example.demo.domain.Professor;
import com.example.demo.domain.Student;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.PasswordMigrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordMigrationService passwordMigrationService;

    // 학생 회원가입
    @PostMapping("/register/student")
    public ResponseEntity<Map<String, Object>> registerStudent(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String studentId = request.get("studentId");
            String name = request.get("name");
            String password = request.get("password");

            if (studentId == null || studentId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "학번을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이름을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean result = authenticationService.registerStudent(studentId.trim(), name.trim(), password);

            if (result) {
                response.put("success", true);
                response.put("message", "회원가입이 완료되었습니다. 관리자 승인을 기다려주세요.");
            } else {
                response.put("success", false);
                response.put("message", "회원가입에 실패했습니다.");
            }

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 교수 회원가입
    @PostMapping("/register/professor")
    public ResponseEntity<Map<String, Object>> registerProfessor(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String professorId = request.get("professorId");
            String name = request.get("name");
            String password = request.get("password");

            if (professorId == null || professorId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "교수ID를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이름을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean result = authenticationService.registerProfessor(professorId.trim(), name.trim(), password);

            if (result) {
                response.put("success", true);
                response.put("message", "회원가입이 완료되었습니다. 관리자 승인을 기다려주세요.");
            } else {
                response.put("success", false);
                response.put("message", "회원가입에 실패했습니다.");
            }

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 관리자 회원가입 (시스템 초기 설정용)
    @PostMapping("/register/admin")
    public ResponseEntity<Map<String, Object>> registerAdministrator(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String administratorId = request.get("administratorId");
            String name = request.get("name");
            String password = request.get("password");

            if (administratorId == null || administratorId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "관리자ID를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이름을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean result = authenticationService.registerAdministrator(administratorId.trim(), name.trim(), password);

            if (result) {
                response.put("success", true);
                response.put("message", "관리자 계정이 생성되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "관리자 계정 생성에 실패했습니다.");
            }

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 학생 로그인
    @PostMapping("/login/student")
    public ResponseEntity<Map<String, Object>> loginStudent(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String studentId = request.get("studentId");
            String password = request.get("password");

            if (studentId == null || studentId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "학번을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            Student student = authenticationService.authenticateStudent(studentId.trim(), password);

            if (student != null) {
                session.setAttribute("userType", "student");
                session.setAttribute("userId", student.getStudentId());
                session.setAttribute("userName", student.getName());

                response.put("success", true);
                response.put("message", "로그인 성공");
                response.put("userType", "student");
                response.put("userId", student.getStudentId());
                response.put("userName", student.getName());
            } else {
                response.put("success", false);
                response.put("message", "학번 또는 비밀번호가 잘못되었습니다.");
            }

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "로그인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 교수 로그인
    @PostMapping("/login/professor")
    public ResponseEntity<Map<String, Object>> loginProfessor(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String professorId = request.get("professorId");
            String password = request.get("password");

            if (professorId == null || professorId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "교수ID를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            Professor professor = authenticationService.authenticateProfessor(professorId.trim(), password);

            if (professor != null) {
                session.setAttribute("userType", "professor");
                session.setAttribute("userId", professor.getProfessorId());
                session.setAttribute("userName", professor.getName());

                response.put("success", true);
                response.put("message", "로그인 성공");
                response.put("userType", "professor");
                response.put("userId", professor.getProfessorId());
                response.put("userName", professor.getName());
            } else {
                response.put("success", false);
                response.put("message", "교수ID 또는 비밀번호가 잘못되었습니다.");
            }

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "로그인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 관리자 로그인
    @PostMapping("/login/admin")
    public ResponseEntity<Map<String, Object>> loginAdministrator(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String administratorId = request.get("administratorId");
            String password = request.get("password");

            if (administratorId == null || administratorId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "관리자ID를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            Administrator administrator = authenticationService.authenticateAdministrator(administratorId.trim(), password);

            if (administrator != null) {
                session.setAttribute("userType", "administrator");
                session.setAttribute("userId", administrator.getAdministratorId());
                session.setAttribute("userName", administrator.getName());

                response.put("success", true);
                response.put("message", "로그인 성공");
                response.put("userType", "administrator");
                response.put("userId", administrator.getAdministratorId());
                response.put("userName", administrator.getName());
            } else {
                response.put("success", false);
                response.put("message", "관리자ID 또는 비밀번호가 잘못되었습니다.");
            }

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "로그인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            session.invalidate();
            response.put("success", true);
            response.put("message", "로그아웃 되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "로그아웃 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }

    // 비밀번호 변경 - 학생
    @PostMapping("/change-password/student")
    public ResponseEntity<Map<String, Object>> changeStudentPassword(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userType = (String) session.getAttribute("userType");
            String userId = (String) session.getAttribute("userId");

            if (!"student".equals(userType) || userId == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "현재 비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "새 비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean result = authenticationService.changeStudentPassword(userId, currentPassword, newPassword);

            if (result) {
                response.put("success", true);
                response.put("message", "비밀번호가 변경되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "비밀번호 변경에 실패했습니다.");
            }

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 비밀번호 변경 - 교수
    @PostMapping("/change-password/professor")
    public ResponseEntity<Map<String, Object>> changeProfessorPassword(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userType = (String) session.getAttribute("userType");
            String userId = (String) session.getAttribute("userId");

            if (!"professor".equals(userType) || userId == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "현재 비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "새 비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean result = authenticationService.changeProfessorPassword(userId, currentPassword, newPassword);

            if (result) {
                response.put("success", true);
                response.put("message", "비밀번호가 변경되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "비밀번호 변경에 실패했습니다.");
            }

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 비밀번호 변경 - 관리자
    @PostMapping("/change-password/admin")
    public ResponseEntity<Map<String, Object>> changeAdministratorPassword(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userType = (String) session.getAttribute("userType");
            String userId = (String) session.getAttribute("userId");

            if (!"administrator".equals(userType) || userId == null) {
                response.put("success", false);
                response.put("message", "관리자 권한이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "현재 비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "새 비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean result = authenticationService.changeAdministratorPassword(userId, currentPassword, newPassword);

            if (result) {
                response.put("success", true);
                response.put("message", "비밀번호가 변경되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "비밀번호 변경에 실패했습니다.");
            }

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 현재 로그인 상태 확인
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        String userType = (String) session.getAttribute("userType");
        String userId = (String) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        if (userType != null && userId != null) {
            response.put("isLoggedIn", true);
            response.put("userType", userType);
            response.put("userId", userId);
            response.put("userName", userName);
        } else {
            response.put("isLoggedIn", false);
        }

        return ResponseEntity.ok(response);
    }

    // 비밀번호 마이그레이션 (개발/관리자용)
    @PostMapping("/migrate-passwords")
    public ResponseEntity<Map<String, Object>> migratePasswords(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userType = (String) session.getAttribute("userType");

            if (!"administrator".equals(userType)) {
                response.put("success", false);
                response.put("message", "관리자 권한이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            passwordMigrationService.migrateAllPasswords();

            response.put("success", true);
            response.put("message", "비밀번호 마이그레이션이 완료되었습니다.");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "마이그레이션 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }
}