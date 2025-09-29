package com.example.demo.service;

import com.example.demo.domain.Administrator;
import com.example.demo.domain.Professor;
import com.example.demo.domain.Student;
import com.example.demo.repository.AdministratorRepository;
import com.example.demo.repository.ProfessorRepository;
import com.example.demo.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthenticationService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    // 비밀번호 해시화 메서드 (public으로 추가)
    public String hashPassword(String password) {
        validatePassword(password);
        return passwordEncoder.encode(password);
    }

    // 학생 회원가입
    public boolean registerStudent(String studentId, String name, String password) {
        // 이미 존재하는 학번인지 확인
        if (studentRepository.existsByStudentId(studentId)) {
            throw new RuntimeException("이미 존재하는 학번입니다.");
        }

        // 비밀번호 유효성 검사
        validatePassword(password);

        try {
            Student student = new Student();
            student.setStudentId(studentId);
            student.setName(name);
            student.setPassword(passwordEncoder.encode(password)); // 비밀번호 해시화
            student.setAllowed(false); // 기본값: 승인 대기

            studentRepository.save(student);
            return true;
        } catch (Exception e) {
            System.err.println("Error registering student: " + e.getMessage());
            return false;
        }
    }

    // 교수 회원가입
    public boolean registerProfessor(String professorId, String name, String password) {
        // 이미 존재하는 교수ID인지 확인
        if (professorRepository.existsByProfessorId(professorId)) {
            throw new RuntimeException("이미 존재하는 교수ID입니다.");
        }

        // 비밀번호 유효성 검사
        validatePassword(password);

        try {
            Professor professor = new Professor();
            professor.setProfessorId(professorId);
            professor.setName(name);
            professor.setPassword(passwordEncoder.encode(password)); // 비밀번호 해시화
            professor.setAllowed(false); // 기본값: 승인 대기

            professorRepository.save(professor);
            return true;
        } catch (Exception e) {
            System.err.println("Error registering professor: " + e.getMessage());
            return false;
        }
    }

    // 관리자 회원가입 (보통은 시스템에 미리 생성되어 있음)
    public boolean registerAdministrator(String administratorId, String name, String password) {
        // 이미 존재하는 관리자ID인지 확인
        if (administratorRepository.existsByAdministratorId(administratorId)) {
            throw new RuntimeException("이미 존재하는 관리자ID입니다.");
        }

        // 비밀번호 유효성 검사
        validatePassword(password);

        try {
            Administrator administrator = new Administrator();
            administrator.setAdministratorId(administratorId);
            administrator.setName(name);
            administrator.setPassword(passwordEncoder.encode(password)); // 비밀번호 해시화

            administratorRepository.save(administrator);
            return true;
        } catch (Exception e) {
            System.err.println("Error registering administrator: " + e.getMessage());
            return false;
        }
    }

    // 학생 로그인
    public Student authenticateStudent(String studentId, String password) {
        Student student = studentRepository.findByStudentId(studentId);

        if (student == null) {
            return null; // 학생을 찾을 수 없음
        }

        // 승인되지 않은 학생
        if (!student.isAllowed()) {
            throw new RuntimeException("관리자의 승인을 기다리고 있습니다.");
        }

        // 비밀번호 검증
        if (passwordEncoder.matches(password, student.getPassword())) {
            return student;
        }

        return null; // 비밀번호 불일치
    }

    // 교수 로그인
    public Professor authenticateProfessor(String professorId, String password) {
        Professor professor = professorRepository.findByProfessorId(professorId);

        if (professor == null) {
            return null; // 교수를 찾을 수 없음
        }

        // 승인되지 않은 교수
        if (!professor.isAllowed()) {
            throw new RuntimeException("관리자의 승인을 기다리고 있습니다.");
        }

        // 비밀번호 검증
        if (passwordEncoder.matches(password, professor.getPassword())) {
            return professor;
        }

        return null; // 비밀번호 불일치
    }

    // 관리자 로그인
    public Administrator authenticateAdministrator(String administratorId, String password) {
        Administrator administrator = administratorRepository.findByAdministratorId(administratorId);

        if (administrator == null) {
            return null; // 관리자를 찾을 수 없음
        }

        // 비밀번호 검증
        if (passwordEncoder.matches(password, administrator.getPassword())) {
            return administrator;
        }

        return null; // 비밀번호 불일치
    }

    // 비밀번호 변경 - 학생
    public boolean changeStudentPassword(String studentId, String currentPassword, String newPassword) {
        Student student = studentRepository.findByStudentId(studentId);

        if (student == null) {
            return false;
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, student.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사
        validatePassword(newPassword);

        try {
            student.setPassword(passwordEncoder.encode(newPassword));
            studentRepository.save(student);
            return true;
        } catch (Exception e) {
            System.err.println("Error changing student password: " + e.getMessage());
            return false;
        }
    }

    // 비밀번호 변경 - 교수
    public boolean changeProfessorPassword(String professorId, String currentPassword, String newPassword) {
        Professor professor = professorRepository.findByProfessorId(professorId);

        if (professor == null) {
            return false;
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, professor.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사
        validatePassword(newPassword);

        try {
            professor.setPassword(passwordEncoder.encode(newPassword));
            professorRepository.save(professor);
            return true;
        } catch (Exception e) {
            System.err.println("Error changing professor password: " + e.getMessage());
            return false;
        }
    }

    // 비밀번호 변경 - 관리자
    public boolean changeAdministratorPassword(String administratorId, String currentPassword, String newPassword) {
        Administrator administrator = administratorRepository.findByAdministratorId(administratorId);

        if (administrator == null) {
            return false;
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, administrator.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사
        validatePassword(newPassword);

        try {
            administrator.setPassword(passwordEncoder.encode(newPassword));
            administratorRepository.save(administrator);
            return true;
        } catch (Exception e) {
            System.err.println("Error changing administrator password: " + e.getMessage());
            return false;
        }
    }

    // 비밀번호 유효성 검사
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("비밀번호를 입력해주세요.");
        }

        if (password.length() < 8) {
            throw new RuntimeException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (password.length() > 50) {
            throw new RuntimeException("비밀번호는 50자 이하여야 합니다.");
        }

        // 영문자, 숫자 포함 검사
        boolean hasLetter = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            }
        }

        if (!hasLetter || !hasNumber) {
            throw new RuntimeException("비밀번호는 영문자와 숫자를 모두 포함해야 합니다.");
        }
    }
}