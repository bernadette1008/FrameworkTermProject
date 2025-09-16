package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    // 학생의 수강 과목 목록 조회 (수정됨)
    public List<Course> getStudentCourses(String studentId) {
        Student student = studentRepository.findByStudentId(studentId);
        if (student == null) {
            throw new RuntimeException("학생을 찾을 수 없습니다.");
        }

        try {
            // 먼저 학생의 등록 정보를 조회
            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

            // 등록 정보에서 수업 코드를 추출하여 수업 정보 조회
            List<String> courseCodes = enrollments.stream()
                    .map(Enrollment::getCourseCode)
                    .collect(Collectors.toList());

            if (courseCodes.isEmpty()) {
                return List.of(); // 빈 리스트 반환
            }

            // 수업 정보 조회
            List<Course> courses = courseRepository.findAllById(courseCodes);

            // 각 과정에 대해 교수 정보와 과제 정보 로드
            for (Course course : courses) {
                try {
                    // 과제 수 설정 (lazy loading 문제 방지)
                    List<Assignment> assignments = assignmentRepository.findByCourseCode(course.getCourseCode());
                    course.setAssignments(assignments);
                } catch (Exception e) {
                    // 과제 로딩 실패 시 빈 리스트로 설정
                    course.setAssignments(List.of());
                }
            }

            return courses;
        } catch (Exception e) {
            // 오류 발생 시 로그 출력 후 빈 리스트 반환
            System.err.println("Error loading student courses: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // 학생의 과제 목록 조회
    public List<Assignment> getStudentAssignments(String studentId) {
        try {
            return assignmentRepository.findByStudentId(studentId);
        } catch (Exception e) {
            System.err.println("Error loading student assignments: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Assignment> getUnsubmittedAssignments(String studentId) {
        // 1. 학생이 수강 중인 모든 과목의 모든 과제 가져오기
        List<Assignment> assignments = getStudentAssignments(studentId);


        // 2. 학생이 이미 제출한 과제 목록 가져오기
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);
        Set<Integer> submittedAssignmentIds = submissions.stream()
                .map(submission -> submission.getAssignment().getAssignmentCode())
                .collect(Collectors.toSet());

        // 3. 제출하지 않은 과제만 필터링
        return assignments.stream()
                .filter(assignment -> !submittedAssignmentIds.contains(assignment.getAssignmentCode()))
                .collect(Collectors.toList());
    }

    // 특정 과제의 상세 정보 조회
    public Assignment getAssignmentDetails(int assignmentCode) {
        return assignmentRepository.findById(assignmentCode)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));
    }

    // 학생의 특정 과제에 대한 제출물 조회
    public Optional<Submission> getSubmission(int assignmentCode, String studentId) {
        return submissionRepository.findByAssignmentCodeAndStudentId(assignmentCode, studentId);
    }

    // 과제 제출
    public Submission submitAssignment(int assignmentCode, String studentId, String content) {
        Assignment assignment = getAssignmentDetails(assignmentCode);
        Student student = studentRepository.findByStudentId(studentId);

        if (student == null) {
            throw new RuntimeException("학생을 찾을 수 없습니다.");
        }

        // 학생이 해당 과제의 수업에 등록되어 있는지 확인
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseCode(studentId, assignment.getCourseCode());
        if (!isEnrolled) {
            throw new RuntimeException("해당 수업에 등록되어 있지 않습니다.");
        }

        // 마감일 확인
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("과제 제출 기한이 지났습니다.");
        }

        // 이미 제출된 과제인지 확인
        Optional<Submission> existingSubmission = submissionRepository.findByAssignmentCodeAndStudentId(assignmentCode, studentId);
        if (existingSubmission.isPresent()) {
            throw new RuntimeException("이미 제출된 과제입니다.");
        }

        Submission submission = new Submission();
        submission.setAssignmentCode(assignmentCode);
        submission.setStudentId(studentId);
        submission.setContent(content);
        submission.setSubmissionTime(LocalDateTime.now());
        submission.setLastModifiedDate(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    // 제출물 수정
    public Submission updateSubmission(int submissionCode, String content) {
        Submission submission = submissionRepository.findBySubmissionCode(submissionCode)
                .orElseThrow(() -> new RuntimeException("제출물을 찾을 수 없습니다."));

        // 마감일 확인
        Assignment assignment = getAssignmentDetails(submission.getAssignmentCode());
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("과제 제출 기한이 지나 수정할 수 없습니다.");
        }

        submission.setContent(content);
        submission.setLastModifiedDate(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    // 제출물 삭제
    public void deleteSubmission(int submissionCode) {
        Submission submission = submissionRepository.findBySubmissionCode(submissionCode)
                .orElseThrow(() -> new RuntimeException("제출물을 찾을 수 없습니다."));

        // 마감일 확인
        Assignment assignment = getAssignmentDetails(submission.getAssignmentCode());
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("과제 제출 기한이 지나 삭제할 수 없습니다.");
        }

        submissionRepository.deleteById(submissionCode);
    }

    // 질문 등록
    public Question submitQuestion(int assignmentCode, String studentId, String content) {
        Assignment assignment = getAssignmentDetails(assignmentCode);
        Student student = studentRepository.findByStudentId(studentId);

        if (student == null) {
            throw new RuntimeException("학생을 찾을 수 없습니다.");
        }

        // 학생이 해당 과제의 수업에 등록되어 있는지 확인
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseCode(studentId, assignment.getCourseCode());
        if (!isEnrolled) {
            throw new RuntimeException("해당 수업에 등록되어 있지 않습니다.");
        }

        Question question = new Question();
        question.setAssignmentCode(assignmentCode);
        question.setStudentId(studentId);
        question.setContent(content);
        question.setQuestionTime(LocalDateTime.now());

        return questionRepository.save(question);
    }

    // 특정 과제의 질문 목록 조회 (답변 포함)
    public List<Question> getAssignmentQuestions(int assignmentCode, String studentId) {
        Assignment assignment = getAssignmentDetails(assignmentCode);
        Student student = studentRepository.findByStudentId(studentId);

        if (student == null) {
            throw new RuntimeException("학생을 찾을 수 없습니다.");
        }

        // 학생이 해당 과제의 수업에 등록되어 있는지 확인
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseCode(studentId, assignment.getCourseCode());
        if (!isEnrolled) {
            throw new RuntimeException("해당 수업에 등록되어 있지 않습니다.");
        }

        List<Question> questions = questionRepository.findByAssignmentCodeAndStudentId(assignmentCode, studentId);

        // 각 질문에 대한 답변 로드
        for (Question question : questions) {
            List<Answer> answers = answerRepository.findByQuestionCodeOrderByAnswerTimeAsc(question.getQuestionCode());
            question.setAnswers(answers);
        }

        return questions;
    }

    // 수업 등록 (수업 코드로) - 수정됨
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

        try {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudentId(studentId);
            enrollment.setCourseCode(courseCode);
            enrollment.setEnrollmentDate(LocalDateTime.now());

            enrollmentRepository.save(enrollment);
            return true;
        } catch (Exception e) {
            System.err.println("Error enrolling in course: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("수업 등록 중 오류가 발생했습니다.");
        }
    }

    // 수업 등록 해제
    @Transactional
    public void unenrollFromCourse(String studentId, String courseCode) {
        if (!enrollmentRepository.existsByStudentIdAndCourseCode(studentId, courseCode)) {
            throw new RuntimeException("등록되지 않은 수업입니다.");
        }

        try {
            enrollmentRepository.deleteByStudentIdAndCourseCode(studentId, courseCode);
        } catch (Exception e) {
            System.err.println("Error unenrolling from course: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("수업 등록 해제 중 오류가 발생했습니다.");
        }
    }

    // 학생의 제출물 목록 조회
    public List<Submission> getStudentSubmissions(String studentId) {
        try {
            return submissionRepository.findByStudentId(studentId);
        } catch (Exception e) {
            System.err.println("Error loading student submissions: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // 학생의 질문 목록 조회
    public List<Question> getStudentQuestions(String studentId) {
        try {
            List<Question> questions = questionRepository.findByStudentId(studentId);

            // 각 질문에 대한 답변 로드
            for (Question question : questions) {
                List<Answer> answers = answerRepository.findByQuestionCodeOrderByAnswerTimeAsc(question.getQuestionCode());
                question.setAnswers(answers);
            }

            return questions;
        } catch (Exception e) {
            System.err.println("Error loading student questions: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}