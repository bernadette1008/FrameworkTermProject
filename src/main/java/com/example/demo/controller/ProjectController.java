package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import com.example.demo.util.XSSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ProjectController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // === 로그인 및 인증 ===

    // 메인 로그인 페이지
    @GetMapping("/")
    public String loginForm(Model model){
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String userId,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        // 관리자 테이블에서 먼저 확인
        Administrator administrator = administratorRepository.findByAdministratorIdAndPassword(userId, password);
        if (administrator != null) {
            // 관리자 로그인 성공
            session.setAttribute("user", administrator);
            session.setAttribute("userType", "관리자");
            return "redirect:/admin/main";  // 변경된 경로
        }

        // 학생 테이블에서 확인
        Student student = studentRepository.findByStudentIdAndPassword(userId, password);
        if (student != null) {
            // 승인 여부 확인
            if (!student.isAllowed()) {
                model.addAttribute("error", "아직 관리자의 승인을 받지 않았습니다. 승인 후 로그인해주세요.");
                return "login";
            }
            // 학생 로그인 성공
            session.setAttribute("user", student);
            session.setAttribute("userType", "학생");
            session.setAttribute("userId", student.getStudentId()); // API용 세션 추가
            return "redirect:/student/main"; // 경로 변경
        }

        // 교수 테이블에서 확인
        Professor professor = professorRepository.findByProfessorIdAndPassword(userId, password);
        if (professor != null) {
            // 승인 여부 확인
            if (!professor.isAllowed()) {
                model.addAttribute("error", "아직 관리자의 승인을 받지 않았습니다. 승인 후 로그인해주세요.");
                return "login";
            }
            // 교수 로그인 성공
            session.setAttribute("user", professor);
            session.setAttribute("userType", "교수");
            return "redirect:/professor-main";
        }

        // 로그인 실패
        model.addAttribute("error", "아이디 또는 비밀번호가 틀렸습니다.");
        return "login";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // === 회원가입 관련 ===

    // 기존의 /student-main 경로도 유지 (호환성을 위해)
    @GetMapping("/student-main")
    public String studentMainRedirect() {
        return "redirect:/student/main";
    }

    // 회원가입 타입 선택 페이지
    @GetMapping("/register-type")
    public String registerTypeForm(Model model){
        return "register-type";
    }

    // 학생 회원가입 페이지
    @GetMapping("/register-student")
    public String registerStudentForm(Model model){
        return "register-student";
    }

    // 교수 회원가입 페이지
    @GetMapping("/register-professor")
    public String registerProfessorForm(Model model){
        return "register-professor";
    }

    // 학생 회원가입 처리
    @PostMapping("/register-student")
    public String processStudentRegister(@RequestParam String name,
                                         @RequestParam String studentNumber,
                                         @RequestParam String password,
                                         Model model) {

        if (studentRepository.existsByStudentId(studentNumber) || professorRepository.existsByProfessorId(studentNumber)) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "register-student";
        }

        Student newStudent = new Student();
        newStudent.setStudentId(studentNumber);
        newStudent.setName(name);
        newStudent.setPassword(password);
        newStudent.setAllowed(false); // 기본값은 승인되지 않음

        studentRepository.save(newStudent);

        model.addAttribute("userType", "학생");
        model.addAttribute("userName", name);
        model.addAttribute("message", "회원가입이 완료되었습니다. 관리자의 승인을 기다려주세요.");

        return "register-pending";
    }

    // 교수 회원가입 처리
    @PostMapping("/register-professor")
    public String processProfessorRegister(@RequestParam String name,
                                           @RequestParam String professorNumber,
                                           @RequestParam String password,
                                           Model model) {

        if (professorRepository.existsByProfessorId(professorNumber) || studentRepository.existsByStudentId(professorNumber)) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "register-professor";
        }

        Professor newProfessor = new Professor();
        newProfessor.setProfessorId(professorNumber);
        newProfessor.setName(name);
        newProfessor.setPassword(password);
        newProfessor.setAllowed(false); // 기본값은 승인되지 않음

        professorRepository.save(newProfessor);

        model.addAttribute("userType", "교수");
        model.addAttribute("userName", name);
        model.addAttribute("message", "회원가입이 완료되었습니다. 관리자의 승인을 기다려주세요.");

        return "register-pending";
    }

    // 회원가입 대기 페이지
    @GetMapping("/register-pending")
    public String registerPendingForm(Model model){
        return "register-pending";
    }

    // 회원가입 성공 페이지
    @GetMapping("/register-success")
    public String registerSuccessForm(Model model){
        return "register-success";
    }

    // === 관리자 관련 ===

    // 관리자 메인 페이지로의 리다이렉트 (호환성을 위해)
    @GetMapping("/administrator-main")
    public String administratorMainRedirect() {
        return "redirect:/admin/main";
    }

    // === 교수 관련 기능들 ===

    // 교수 메인 페이지
    @GetMapping("/professor-main")
    public String professorMain(Model model, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        // 교수가 담당하는 강의 목록 조회
        List<Course> courses = courseRepository.findByProfessorId(professor.getProfessorId());
        // 교수가 등록한 과제 목록 조회
        List<Assignment> assignments = assignmentRepository.findByProfessorId(professor.getProfessorId());

        int enrollments = 0;

        for(Course course : courses) {
            enrollments += enrollmentRepository.findByCourseCode(course.getCourseCode()).size();
        }

        model.addAttribute("professor", professor);
        model.addAttribute("courses", courses);
        model.addAttribute("assignments", assignments);
        model.addAttribute("enrollments", enrollments);

        return "professor/professor-main";
    }

    // 강의 생성 페이지
    @GetMapping("/create-course")
    public String createCourseForm(Model model, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }
        model.addAttribute("professor", professor);
        return "professor/create-course";
    }

    @PostMapping("/create-course")
    public String processCreateCourse(@RequestParam String courseName,
                                      @RequestParam String courseCode,
                                      HttpSession session,
                                      Model model) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            // 입력값 검증
            if (courseName == null || courseName.trim().isEmpty()) {
                model.addAttribute("professor", professor);
                model.addAttribute("error", "강의명을 입력해주세요.");
                return "professor/create-course";
            }

            if (courseCode == null || courseCode.trim().isEmpty()) {
                model.addAttribute("professor", professor);
                model.addAttribute("error", "강의 코드를 입력해주세요.");
                return "professor/create-course";
            }

            // XSS 검증
            XSSUtils.validateInput(courseName, "강의명");
            XSSUtils.validateInput(courseCode, "강의 코드");

            // 길이 제한
            if (courseName.length() > 100) {
                model.addAttribute("professor", professor);
                model.addAttribute("error", "강의명이 너무 깁니다. (최대 100자)");
                return "professor/create-course";
            }

            if (courseCode.length() > 20) {
                model.addAttribute("professor", professor);
                model.addAttribute("error", "강의 코드가 너무 깁니다. (최대 20자)");
                return "professor/create-course";
            }

            // 강의 코드 형식 검증 (영문, 숫자, 하이픈, 언더스코어만 허용)
            if (!courseCode.matches("^[A-Za-z0-9_-]+$")) {
                model.addAttribute("professor", professor);
                model.addAttribute("error", "강의 코드는 영문, 숫자, 하이픈(-), 언더스코어(_)만 사용할 수 있습니다.");
                return "professor/create-course";
            }

            // 강의 코드 중복 체크
            if (courseRepository.existsByCourseCode(courseCode.trim())) {
                model.addAttribute("professor", professor);
                model.addAttribute("error", "이미 사용 중인 강의 코드입니다.");
                return "professor/create-course";
            }

            Course newCourse = new Course();
            newCourse.setCourseName(XSSUtils.sanitizeInput(courseName.trim()));
            newCourse.setCourseCode(courseCode.trim().toUpperCase()); // 강의코드는 대문자로 통일
            newCourse.setProfessorId(professor.getProfessorId());

            courseRepository.save(newCourse);

            return "redirect:/professor-main?courseCreated=true";

        } catch (IllegalArgumentException e) {
            // XSS 검증 실패
            model.addAttribute("professor", professor);
            model.addAttribute("error", e.getMessage());
            return "professor/create-course";
        } catch (Exception e) {
            model.addAttribute("professor", professor);
            model.addAttribute("error", "강의 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "professor/create-course";
        }
    }

    // 과제 생성 페이지
    @GetMapping("/create-assignment")
    public String createAssignmentForm(Model model, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        // 교수가 담당하는 강의 목록
        List<Course> courses = courseRepository.findByProfessorId(professor.getProfessorId());

        model.addAttribute("professor", professor);
        model.addAttribute("courses", courses);
        return "professor/create-assignment";
    }

    @PostMapping("/create-assignment")
    public String processCreateAssignment(@RequestParam String courseId,
                                          @RequestParam String title,
                                          @RequestParam String content,
                                          @RequestParam String dueDate,
                                          @RequestParam String dueTime,
                                          HttpSession session,
                                          Model model) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        try {
            // XSS 검증
            XSSUtils.validateInput(title, "과제 제목");
            XSSUtils.validateInput(content, "과제 내용");

            // 입력값 길이 제한
            if (title.length() > 200) {
                model.addAttribute("error", "과제 제목이 너무 깁니다. (최대 200자)");
                List<Course> courses = courseRepository.findByProfessorId(professor.getProfessorId());
                model.addAttribute("professor", professor);
                model.addAttribute("courses", courses);
                return "professor/create-assignment";
            }

            if (content.length() > 5000) {
                model.addAttribute("error", "과제 내용이 너무 깁니다. (최대 5,000자)");
                List<Course> courses = courseRepository.findByProfessorId(professor.getProfessorId());
                model.addAttribute("professor", professor);
                model.addAttribute("courses", courses);
                return "professor/create-assignment";
            }

            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                model.addAttribute("error", "잘못된 강의를 선택했습니다.");
                List<Course> courses = courseRepository.findByProfessorId(professor.getProfessorId());
                model.addAttribute("professor", professor);
                model.addAttribute("courses", courses);
                return "professor/create-assignment";
            }

            // 교수 권한 확인 (해당 강의의 담당 교수인지)
            if (!course.getProfessorId().equals(professor.getProfessorId())) {
                model.addAttribute("error", "해당 강의에 대한 권한이 없습니다.");
                List<Course> courses = courseRepository.findByProfessorId(professor.getProfessorId());
                model.addAttribute("professor", professor);
                model.addAttribute("courses", courses);
                return "professor/create-assignment";
            }

            Assignment newAssignment = new Assignment();
            newAssignment.setCourseCode(courseId);
            newAssignment.setCourse(course);
            newAssignment.setTitle(XSSUtils.sanitizeInput(title.trim())); // XSS 정제
            newAssignment.setContent(XSSUtils.sanitizeInput(content.trim())); // XSS 정제

            // 날짜와 시간을 합쳐서 LocalDateTime으로 변환
            LocalDate date = LocalDate.parse(dueDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime time = LocalTime.parse(dueTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime dueDateTime = LocalDateTime.of(date, time);

            // 마감일이 현재 시간보다 이전인지 확인
            if (dueDateTime.isBefore(LocalDateTime.now())) {
                model.addAttribute("error", "마감일은 현재 시간보다 이후여야 합니다.");
                List<Course> courses = courseRepository.findByProfessorId(professor.getProfessorId());
                model.addAttribute("professor", professor);
                model.addAttribute("courses", courses);
                return "professor/create-assignment";
            }

            newAssignment.setDueDate(dueDateTime);
            newAssignment.setCreatedDate(LocalDateTime.now());

            assignmentRepository.save(newAssignment);

            return "redirect:/professor-main?assignmentCreated=true";

        } catch (IllegalArgumentException e) {
            // XSS 검증 실패
            model.addAttribute("error", e.getMessage());
            List<Course> courses = courseRepository.findByProfessorId(professor.getProfessorId());
            model.addAttribute("professor", professor);
            model.addAttribute("courses", courses);
            return "professor/create-assignment";
        } catch (Exception e) {
            model.addAttribute("error", "과제 생성 중 오류가 발생했습니다: " + e.getMessage());
            List<Course> courses = courseRepository.findByProfessorId(professor.getProfessorId());
            model.addAttribute("professor", professor);
            model.addAttribute("courses", courses);
            return "professor/create-assignment";
        }
    }
}