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

import java.util.List;

@Service
@Transactional
public class PasswordMigrationService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    /**
     * 모든 사용자의 평문 비밀번호를 해시화된 비밀번호로 마이그레이션
     * 주의: 이 메서드는 기존 시스템에 평문 비밀번호가 있을 때만 사용해야 합니다.
     */
    public void migrateAllPasswords() {
        System.out.println("비밀번호 마이그레이션을 시작합니다...");

        try {
            migrateStudentPasswords();
            migrateProfessorPasswords();
            migrateAdministratorPasswords();

            System.out.println("비밀번호 마이그레이션이 완료되었습니다.");
        } catch (Exception e) {
            System.err.println("비밀번호 마이그레이션 중 오류가 발생했습니다: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 학생 비밀번호 마이그레이션
     */
    @Transactional
    public void migrateStudentPasswords() {
        System.out.println("학생 비밀번호 마이그레이션을 시작합니다...");

        List<Student> students = studentRepository.findAll();
        int migratedCount = 0;

        for (Student student : students) {
            try {
                String currentPassword = student.getPassword();

                // 이미 해시화된 비밀번호인지 확인 (BCrypt 해시는 보통 $2a$로 시작)
                if (currentPassword != null && !currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2b$")) {
                    // 평문 비밀번호를 해시화
                    String hashedPassword = passwordEncoder.encode(currentPassword);
                    student.setPassword(hashedPassword);
                    studentRepository.save(student);
                    migratedCount++;

                    System.out.println("학생 " + student.getStudentId() + "의 비밀번호를 마이그레이션했습니다.");
                }
            } catch (Exception e) {
                System.err.println("학생 " + student.getStudentId() + " 비밀번호 마이그레이션 실패: " + e.getMessage());
            }
        }

        System.out.println("학생 비밀번호 마이그레이션 완료: " + migratedCount + "/" + students.size());
    }

    /**
     * 교수 비밀번호 마이그레이션
     */
    @Transactional
    public void migrateProfessorPasswords() {
        System.out.println("교수 비밀번호 마이그레이션을 시작합니다...");

        List<Professor> professors = professorRepository.findAll();
        int migratedCount = 0;

        for (Professor professor : professors) {
            try {
                String currentPassword = professor.getPassword();

                // 이미 해시화된 비밀번호인지 확인
                if (currentPassword != null && !currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2b$")) {
                    // 평문 비밀번호를 해시화
                    String hashedPassword = passwordEncoder.encode(currentPassword);
                    professor.setPassword(hashedPassword);
                    professorRepository.save(professor);
                    migratedCount++;

                    System.out.println("교수 " + professor.getProfessorId() + "의 비밀번호를 마이그레이션했습니다.");
                }
            } catch (Exception e) {
                System.err.println("교수 " + professor.getProfessorId() + " 비밀번호 마이그레이션 실패: " + e.getMessage());
            }
        }

        System.out.println("교수 비밀번호 마이그레이션 완료: " + migratedCount + "/" + professors.size());
    }

    /**
     * 관리자 비밀번호 마이그레이션
     */
    @Transactional
    public void migrateAdministratorPasswords() {
        System.out.println("관리자 비밀번호 마이그레이션을 시작합니다...");

        List<Administrator> administrators = administratorRepository.findAll();
        int migratedCount = 0;

        for (Administrator administrator : administrators) {
            try {
                String currentPassword = administrator.getPassword();

                // 이미 해시화된 비밀번호인지 확인
                if (currentPassword != null && !currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2b$")) {
                    // 평문 비밀번호를 해시화
                    String hashedPassword = passwordEncoder.encode(currentPassword);
                    administrator.setPassword(hashedPassword);
                    administratorRepository.save(administrator);
                    migratedCount++;

                    System.out.println("관리자 " + administrator.getAdministratorId() + "의 비밀번호를 마이그레이션했습니다.");
                }
            } catch (Exception e) {
                System.err.println("관리자 " + administrator.getAdministratorId() + " 비밀번호 마이그레이션 실패: " + e.getMessage());
            }
        }

        System.out.println("관리자 비밀번호 마이그레이션 완료: " + migratedCount + "/" + administrators.size());
    }

    /**
     * 특정 사용자의 비밀번호를 강제로 재설정 (관리자용)
     */
    public void resetStudentPassword(String studentId, String newPassword) {
        Student student = studentRepository.findByStudentId(studentId);
        if (student == null) {
            throw new RuntimeException("학생을 찾을 수 없습니다: " + studentId);
        }

        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);

        System.out.println("학생 " + studentId + "의 비밀번호가 재설정되었습니다.");
    }

    public void resetProfessorPassword(String professorId, String newPassword) {
        Professor professor = professorRepository.findByProfessorId(professorId);
        if (professor == null) {
            throw new RuntimeException("교수를 찾을 수 없습니다: " + professorId);
        }

        professor.setPassword(passwordEncoder.encode(newPassword));
        professorRepository.save(professor);

        System.out.println("교수 " + professorId + "의 비밀번호가 재설정되었습니다.");
    }

    public void resetAdministratorPassword(String administratorId, String newPassword) {
        Administrator administrator = administratorRepository.findByAdministratorId(administratorId);
        if (administrator == null) {
            throw new RuntimeException("관리자를 찾을 수 없습니다: " + administratorId);
        }

        administrator.setPassword(passwordEncoder.encode(newPassword));
        administratorRepository.save(administrator);

        System.out.println("관리자 " + administratorId + "의 비밀번호가 재설정되었습니다.");
    }
}