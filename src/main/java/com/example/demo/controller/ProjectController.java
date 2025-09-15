package com.example.demo.controller;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
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
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;

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

        // 학생 테이블에서 확인
        Student student = studentRepository.findByStudentIdAndPassword(userId, password);
        if (student != null) {
            // 학생 로그인 성공
            session.setAttribute("user", student);
            session.setAttribute("userType", "학생");
            session.setAttribute("userId", student.getStudentId()); // API용 세션 추가
            return "redirect:/student/main"; // 경로 변경
        }

        // 교수 테이블에서 확인
        Professor professor = professorRepository.findByProfessorIdAndPassword(userId, password);
        if (professor != null) {
            // 교수 로그인 성공
            session.setAttribute("user", professor);
            session.setAttribute("userType", "교수");
            return "redirect:/professor-main";
        }

        // 로그인 실패
        model.addAttribute("error", "아이디 또는 비밀번호가 틀렸습니다.");
        return "login";
    }

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

        studentRepository.save(newStudent);

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

        if (professorRepository.existsByProfessorId(professorNumber) || studentRepository.existsByStudentId(professorNumber)) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "register-professor";
        }

        Professor newProfessor = new Professor();
        newProfessor.setProfessorId(professorNumber);
        newProfessor.setName(name);
        newProfessor.setPassword(password);

        professorRepository.save(newProfessor);

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
//    @GetMapping("/student-main")
//    public String studentMain(Model model, HttpSession session) {
//        Student student = (Student) session.getAttribute("user");
//        if (student == null) {
//            return "redirect:/";
//        }
//        model.addAttribute("student", student);
//        return "student-main";
//    }

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

        return "professor-main";
    }

    // 강의 생성 페이지
    @GetMapping("/create-course")
    public String createCourseForm(Model model, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }
        model.addAttribute("professor", professor);
        return "create-course";
    }

    // 강의 생성 처리
    @PostMapping("/create-course")
    public String processCreateCourse(@RequestParam String courseName,
                                      @RequestParam String courseCode,
                                      HttpSession session,
                                      Model model) {
        Professor professor = (Professor) session.getAttribute("user");
        if (professor == null) {
            return "redirect:/";
        }

        // 강의 코드 중복 체크
        if (courseRepository.existsByCourseCode(courseCode)) {
            model.addAttribute("error", "이미 사용 중인 강의 코드입니다.");
            return "create-course";
        }

        Course newCourse = new Course();
        newCourse.setCourseName(courseName);
        newCourse.setCourseCode(courseCode);
        newCourse.setProfessorId(professor.getProfessorId());

        courseRepository.save(newCourse);

        return "redirect:/professor-main?courseCreated=true";
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
        return "create-assignment";
    }

    // 과제 생성 처리
//    @PostMapping("/create-assignment")
//    public String processCreateAssignment(@RequestParam String courseId,
//                                          @RequestParam String title,
//                                          @RequestParam String content,
//                                          @RequestParam String dueDate,
//                                          @RequestParam String dueTime,
//                                          HttpSession session,
//                                          Model model) {
//        Professor professor = (Professor) session.getAttribute("user");
//        if (professor == null) {
//            return "redirect:/";
//        }
//
//        // 선택된 강의 정보 가져오기
//        Course course = courseRepository.findById(courseId).orElse(null);
//        if (course == null) {
//            model.addAttribute("error", "잘못된 강의를 선택했습니다.");
//            return "create-assignment";
//        }
//
//        Assignment newAssignment = new Assignment();
//        newAssignment.setCourseCode(courseId);
//        newAssignment.setCourse(course);
//        newAssignment.setTitle(title);
//        newAssignment.setContent(content);
//        newAssignment.setDueDate(LocalDateTime.parse(dueDate));
//        newAssignment.setDueDate(LocalDateTime.parse(dueTime));
//        newAssignment.setCreatedDate(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
//
//        assignmentRepository.save(newAssignment);
//
//        return "redirect:/professor-main?assignmentCreated=true";
//    }

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

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            model.addAttribute("error", "잘못된 강의를 선택했습니다.");
            return "create-assignment";
        }

        Assignment newAssignment = new Assignment();
        newAssignment.setCourseCode(courseId);
        newAssignment.setCourse(course);
        newAssignment.setTitle(title);
        newAssignment.setContent(content);

        // 날짜와 시간을 합쳐서 LocalDateTime으로 변환
        LocalDate date = LocalDate.parse(dueDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalTime time = LocalTime.parse(dueTime, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime dueDateTime = LocalDateTime.of(date, time);
        newAssignment.setDueDate(dueDateTime);

        newAssignment.setCreatedDate(LocalDateTime.now());

        assignmentRepository.save(newAssignment);

        return "redirect:/professor-main?assignmentCreated=true";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}