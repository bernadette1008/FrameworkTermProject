package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // 학생의 수강 과목 목록 조회
    public List<Course> getStudentCourses(String studentId) {
        Student student = studentRepository.findByStudentId(studentId);
        if (student == null) {
            throw new RuntimeException("학생을 찾을 수 없습니다.");
        }

        List<Course> courses = courseRepository.findByStudentId(studentId);
        return courses; // null 체크 제거 - 빈 리스트도 반환
    }

    // 학생의 과제 목록 조회
    public List<Assignment> getStudentAssignments(String studentId) {
        return assignmentRepository.findByStudentId(studentId);
    }

    // 특정 과제의 상세 정보 조회
    public Assignment getAssignmentDetails(String assignmentCode) {
        return assignmentRepository.findById(Integer.valueOf(assignmentCode))
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));
    }

    // 학생의 특정 과제에 대한 제출물 조회
    public Optional<Submission> getSubmission(String assignmentCode, String studentId) {
        return submissionRepository.findByAssignmentCodeAndStudentId(Integer.parseInt(assignmentCode), studentId);
    }

    // 과제 제출
    public Submission submitAssignment(String assignmentCode, String studentId, String content) {
        Assignment assignment = getAssignmentDetails(assignmentCode);
        Student student = studentRepository.findByStudentId(studentId);

        if (student == null) {
            throw new RuntimeException("학생을 찾을 수 없습니다.");
        }

        // 마감일 확인
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("과제 제출 기한이 지났습니다.");
        }

        // 이미 제출된 과제인지 확인
        Optional<Submission> existingSubmission = submissionRepository.findByAssignmentCodeAndStudentId(Integer.parseInt(assignmentCode), studentId);
        if (existingSubmission.isPresent()) {
            throw new RuntimeException("이미 제출된 과제입니다.");
        }

        Submission submission = new Submission();
        submission.setSubmissionCode(Integer.parseInt(generateSubmissionCode()));
        submission.setAssignmentCode(Integer.parseInt(assignmentCode));
        submission.setStudentId(studentId);
        submission.setContent(content);
        submission.setSubmissionTime(LocalDateTime.now());
        submission.setLastModifiedDate(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    // 제출물 수정
    public Submission updateSubmission(String submissionCode, String content) {
        Submission submission = submissionRepository.findById(submissionCode)
                .orElseThrow(() -> new RuntimeException("제출물을 찾을 수 없습니다."));

        // 마감일 확인
        Assignment assignment = getAssignmentDetails(String.valueOf(submission.getAssignmentCode()));
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("과제 제출 기한이 지나 수정할 수 없습니다.");
        }

        submission.setContent(content);
        submission.setLastModifiedDate(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    // 제출물 삭제
    public void deleteSubmission(String submissionCode) {
        Submission submission = submissionRepository.findById(submissionCode)
                .orElseThrow(() -> new RuntimeException("제출물을 찾을 수 없습니다."));

        // 마감일 확인
        Assignment assignment = getAssignmentDetails(String.valueOf(submission.getAssignmentCode()));
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("과제 제출 기한이 지나 삭제할 수 없습니다.");
        }

        submissionRepository.deleteById(submissionCode);
    }

    // 질문 등록
    public Question submitQuestion(String assignmentCode, String studentId, String content) {
        Assignment assignment = getAssignmentDetails(assignmentCode);
        Student student = studentRepository.findByStudentId(studentId);

        if (student == null) {
            throw new RuntimeException("학생을 찾을 수 없습니다.");
        }

        Question question = new Question();
        question.setQuestionCode(generateQuestionCode());
        question.setAssignmentCode(assignmentCode);
        question.setStudentId(studentId);
        question.setContent(content);
        question.setQuestionTime(LocalDateTime.now());

        return questionRepository.save(question);
    }

    // 특정 과제의 질문 목록 조회 (답변 포함)
    public List<Question> getAssignmentQuestions(String assignmentCode, String studentId) {
        Assignment assignment = getAssignmentDetails(assignmentCode);
        Student student = studentRepository.findByStudentId(studentId);

        if (student == null) {
            throw new RuntimeException("학생을 찾을 수 없습니다.");
        }

        List<Question> questions = questionRepository.findByAssignmentCodeAndStudentId(Integer.parseInt(assignmentCode), studentId);

        // 각 질문에 대한 답변 로드
        for (Question question : questions) {
            List<Answer> answers = answerRepository.findByQuestionCodeOrderByAnswerTimeAsc(question.getQuestionCode());
            question.setAnswers(answers);
        }

        return questions;
    }

    // 수업 등록 (수업 코드로)
    public boolean enrollInCourse(String studentId, String courseCode) {
        Student student = studentRepository.findByStudentId(studentId);
        if (student == null) {
            throw new RuntimeException("학생을 찾을 수 없습니다.");
        }

        Course course = courseRepository.findByCourseCode(courseCode);
        if (course == null) {
            return false; // 수업 코드가 잘못됨
        }

        // 이미 등록된 수업인지 확인
        if (enrollmentRepository.existsByStudentIdAndCourseCode(studentId, courseCode)) {
            throw new RuntimeException("이미 등록된 수업입니다.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseCode(courseCode);
        enrollment.setEnrollmentDate(LocalDateTime.now());

        enrollmentRepository.save(enrollment);
        return true;
    }

    // 수업 등록 해제
    @Transactional
    public void unenrollFromCourse(String studentId, String courseCode) {
        if (!enrollmentRepository.existsByStudentIdAndCourseCode(studentId, courseCode)) {
            throw new RuntimeException("등록되지 않은 수업입니다.");
        }

        enrollmentRepository.deleteByStudentIdAndCourseCode(studentId, courseCode);
    }

    // 학생의 제출물 목록 조회
    public List<Submission> getStudentSubmissions(String studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    // 학생의 질문 목록 조회
    public List<Question> getStudentQuestions(String studentId) {
        List<Question> questions = questionRepository.findByStudentId(studentId);

        // 각 질문에 대한 답변 로드
        for (Question question : questions) {
            List<Answer> answers = answerRepository.findByQuestionCodeOrderByAnswerTimeAsc(question.getQuestionCode());
            question.setAnswers(answers);
        }

        return questions;
    }

    // 유니크한 코드 생성 메서드들
    private String generateSubmissionCode() {
        String code;
        do {
            code = "SUB_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (submissionRepository.existsById(code));
        return code;
    }

    private String generateQuestionCode() {
        String code;
        do {
            code = "QST_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (questionRepository.existsById(code));
        return code;
    }
}