package com.example.demo.controller;

import com.example.demo.domain.Student;
import com.example.demo.domain.Professor;
import com.example.demo.repository.studentRepository;
import com.example.demo.repository.professorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProjectController {

    @Autowired
    private studentRepository studentRepository;

    @Autowired
    private professorRepository professorRepository;

    // 메인 로그인 페이지
    @GetMapping("/")
    public String loginForm(Model model){
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String processLogin(@RequestParam String userId,
                               @RequestParam String password,
                               Model model) {

        // 학생 테이블에서 확인
        Student student = studentRepository.findBySidAndPassword(userId, password);
        if (student != null) {
            // 학생 로그인 성공
            model.addAttribute("user", student);
            model.addAttribute("userType", "학생");
            return "redirect:/student-main"; // 학생용 메인 페이지로 리다이렉트
        }

        // 교수 테이블에서 확인
        Professor professor = professorRepository.findByPidAndPassword(userId, password);
        if (professor != null) {
            // 교수 로그인 성공
            model.addAttribute("user", professor);
            model.addAttribute("userType", "교수");
            return "redirect:/professor-main"; // 교수용 메인 페이지로 리다이렉트
        }

        // 로그인 실패
        model.addAttribute("error", "아이디 또는 비밀번호가 틀렸습니다.");
        return "login";
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

        // 아이디 중복 체크 (학생과 교수 모두 같은 P_id 필드 사용하므로 둘 다 확인)
        if (studentRepository.existsBySid(studentNumber) || professorRepository.existsByPid(studentNumber)) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "register-student";
        }

        // 새 학생 생성
        Student newStudent = new Student();
        newStudent.setSid(studentNumber);
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
                                           @RequestParam String studentNumber, // 실제로는 교수 아이디
                                           @RequestParam String password,
                                           Model model) {

        // 아이디 중복 체크 (학생과 교수 모두 같은 P_id 필드 사용하므로 둘 다 확인)
        if (professorRepository.existsByPid(studentNumber) || studentRepository.existsBySid(studentNumber)) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "register-professor";
        }

        // 새 교수 생성
        Professor newProfessor = new Professor();
        newProfessor.setPid(studentNumber);
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

    // 임시 메인 페이지들
    @GetMapping("/student-main")
    public String studentMain(Model model) {
        return "student-main"; // 학생용 메인 페이지 (추후 구현)
    }

    @GetMapping("/professor-main")
    public String professorMain(Model model) {
        return "professor-main"; // 교수용 메인 페이지 (추후 구현)
    }


}