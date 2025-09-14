package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.ProfessorRepository;
import com.example.demo.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ProjectController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private StudentService studentService;

    // 메인 로그인 페이지
    @GetMapping("/")
    public String loginForm(Model model){
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String processLogin(@RequestParam String userId,
                               @RequestParam String password,
                               Model model,
                               HttpSession session) {

        // 학생 테이블에서 확인
        Student student = studentRepository.findByStudentIdAndPassword(userId, password);
        if (student != null) {
            // 학생 로그인 성공 - 세션에 정보 저장
            session.setAttribute("userId", student.getStudentId());
            session.setAttribute("userName", student.getName());
            session.setAttribute("userType", "학생");
            return "redirect:/student-main"; // 학생용 메인 페이지로 리다이렉트
        }

        // 교수 테이블에서 확인
        Professor professor = professorRepository.findByProfessorIdAndPassword(userId, password);
        if (professor != null) {
            // 교수 로그인 성공 - 세션에 정보 저장
            session.setAttribute("userId", professor.getProfessorId());
            session.setAttribute("userName", professor.getName());
            session.setAttribute("userType", "교수");
            return "redirect:/professor-main"; // 교수용 메인 페이지로 리다이렉트
        }

        // 로그인 실패
        model.addAttribute("error", "아이디 또는 비밀번호가 틀렸습니다.");
        return "login";
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
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

        // 아이디 중복 체크 (학생과 교수 모두 확인)
        if (studentRepository.existsByStudentId(studentNumber) || professorRepository.existsByProfessorId(studentNumber)) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "register-student";
        }

        // 새 학생 생성
        Student newStudent = new Student();
        newStudent.setStudentId(studentNumber);
        newStudent.setName(name);
        newStudent.setPassword(password);

        studentRepository.save(newStudent);

        // 성공 페이지로 리다이렉트
        model.addAttribute("userType", "학생");
        model.addAttribute("userName", name);

        return "register-success";
    }

    // 교수 회원가입 처리
    @PostMapping("/register-professor")
    public String processProfessorRegister(@RequestParam String name,
                                           @RequestParam String professorNumber,
                                           @RequestParam String password,
                                           Model model) {

        // 아이디 중복 체크 (학생과 교수 모두 확인)
        if (professorRepository.existsByProfessorId(professorNumber) || studentRepository.existsByStudentId(professorNumber)) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "register-professor";
        }

        // 새 교수 생성
        Professor newProfessor = new Professor();
        newProfessor.setProfessorId(professorNumber);
        newProfessor.setName(name);
        newProfessor.setPassword(password);

        professorRepository.save(newProfessor);

        // 성공 페이지로 리다이렉트
        model.addAttribute("userType", "교수");
        model.addAttribute("userName", name);

        return "register-success";
    }

    // 회원가입 성공 페이지
    @GetMapping("/register-success")
    public String registerSuccessForm(Model model){
        return "register-success";
    }

    // 학생 메인 페이지
    @GetMapping("/student-main")
    public String studentMain(Model model, HttpSession session) {
        String studentId = (String) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        if (studentId == null) {
            return "redirect:/";
        }

        try {
            // 학생의 수강 과목 및 과제 정보 조회
            List<Course> courses = studentService.getStudentCourses(studentId);
            List<Assignment> assignments = studentService.getStudentAssignments(studentId);

            // 과제별 제출 상태 확인
            for (Assignment assignment : assignments) {
                Optional<Submission> submission = studentService.getSubmission(assignment.getAssignmentCode(), studentId);
                assignment.setSubmitted(submission.isPresent());
                assignment.setOverdue(assignment.getDueDate().isBefore(LocalDateTime.now()));
            }

            // 과목별 과제 그룹화
            Map<Course, List<Assignment>> courseAssignments = new HashMap<>();
            for (Course course : courses) {
                List<Assignment> courseAssignmentList = assignments.stream()
                        .filter(assignment -> assignment.getCourseCode().equals(course.getCourseCode()))
                        .collect(Collectors.toList());
                courseAssignments.put(course, courseAssignmentList);
            }

            model.addAttribute("courses", courses);
            model.addAttribute("assignments", assignments);
            model.addAttribute("courseAssignments", courseAssignments);
            model.addAttribute("userName", userName);

        } catch (Exception e) {
            model.addAttribute("error", "데이터를 불러오는데 실패했습니다: " + e.getMessage());
            e.printStackTrace();
        }

        return "student-main";
    }

    @GetMapping("/professor-main")
    public String professorMain(Model model, HttpSession session) {
        String professorId = (String) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        if (professorId == null) {
            return "redirect:/";
        }

        model.addAttribute("userName", userName);
        return "professor-main"; // 교수용 메인 페이지 (추후 구현)
    }
}