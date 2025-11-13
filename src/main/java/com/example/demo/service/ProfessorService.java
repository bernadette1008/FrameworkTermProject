package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import com.example.demo.util.XSSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProfessorService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    // 교수의 과제 목록 조회
    public List<Assignment> getProfessorAssignments(String professorId) {
        return assignmentRepository.findByProfessorIdOrSubProfessorsId(professorId);
    }

    // 특정 과제의 제출물 목록 조회
    public List<Submission> getAssignmentSubmissions(int assignmentCode) {
        return submissionRepository.findByAssignmentCode(assignmentCode);
    }

    // 과제 상세 정보 조회
    public Assignment getAssignmentDetails(int assignmentCode) {
        return assignmentRepository.findById(assignmentCode)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));
    }

    // 제출물 상세 정보 조회
    public Submission getSubmissionDetails(int submissionCode) {
        return submissionRepository.findBySubmissionCode(submissionCode)
                .orElseThrow(() -> new RuntimeException("제출물을 찾을 수 없습니다."));
    }

    // 채점 처리 (피드백 XSS 방어 추가)
    public void gradeSubmission(int submissionCode, Integer score, String feedback) {
        Submission submission = getSubmissionDetails(submissionCode);

        if (score < 0 || score > 100) {
            throw new RuntimeException("점수는 0-100 사이의 값이어야 합니다.");
        }

        // 피드백에 대한 XSS 검증
        if (feedback != null && !feedback.trim().isEmpty()) {
            XSSUtils.validateInput(feedback, "피드백");

            if (feedback.length() > 1000) {
                throw new RuntimeException("피드백이 너무 깁니다. (최대 1,000자)");
            }

            feedback = XSSUtils.sanitizeInput(feedback.trim());
        }

        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setLastModifiedDate(LocalDateTime.now());

        submissionRepository.save(submission);
    }

    // 교수의 질문 목록 조회
    public List<Question> getProfessorQuestions(String professorId) {
        // 교수가 담당하는 과제들의 질문 조회
        List<Assignment> assignments = getProfessorAssignments(professorId);
        List<Question> allQuestions = new java.util.ArrayList<>();

        for (Assignment assignment : assignments) {
            List<Question> questions = questionRepository.findByAssignmentCode(assignment.getAssignmentCode());
            // 각 질문에 대한 답변도 로드
            for (Question question : questions) {
                List<Answer> answers = answerRepository.findByQuestionCodeOrderByAnswerTimeAsc(question.getQuestionCode());
                question.setAnswers(answers);
            }
            allQuestions.addAll(questions);
        }

        return allQuestions;
    }

    // 특정 과제의 질문 목록 조회
    public List<Question> getAssignmentQuestions(int assignmentCode) {
        List<Question> questions = questionRepository.findByAssignmentCode(assignmentCode);

        // 각 질문에 대한 답변도 함께 로드
        for (Question question : questions) {
            List<Answer> answers = answerRepository.findByQuestionCodeOrderByAnswerTimeAsc(question.getQuestionCode());
            question.setAnswers(answers);
        }

        return questions;
    }

    // 특정 수업의 질문 목록 조회
    public List<Question> getCourseQuestions(String courseCode) {
        List<Assignment> assignments = assignmentRepository.findByCourseCode(courseCode);
        List<Question> allQuestions = new ArrayList<>();

        for (Assignment assignment : assignments) {
            List<Question> questions = questionRepository.findByAssignmentCode(assignment.getAssignmentCode());
            allQuestions.addAll(questions);
        }

        // 각 질문에 대한 답변도 함께 로드
        for (Question question : allQuestions) {
            List<Answer> answers = answerRepository.findByQuestionCodeOrderByAnswerTimeAsc(question.getQuestionCode());
            question.setAnswers(answers);
        }

        return allQuestions;
    }

    // 질문 상세 정보 조회
    public Question getQuestionDetails(int questionCode) {
        Question question = questionRepository.findByQuestionCode(questionCode)
                .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));

        // 답변 목록도 함께 로드
        List<Answer> answers = answerRepository.findByQuestionCodeOrderByAnswerTimeAsc(questionCode);
        question.setAnswers(answers);

        return question;
    }

    // 질문 답변 등록 (XSS 방어 추가)
    public Answer answerQuestion(int questionCode, String professorId, String content) {
        // XSS 검증
        XSSUtils.validateInput(content, "답변 내용");

        // 길이 제한
        if (content != null && content.length() > 3000) {
            throw new RuntimeException("답변 내용이 너무 깁니다. (최대 3,000자)");
        }

        Question question = getQuestionDetails(questionCode);
        Professor professor = professorRepository.findByProfessorId(professorId);

        if (professor == null) {
            throw new RuntimeException("교수를 찾을 수 없습니다.");
        }

        Answer answer = new Answer();
        answer.setQuestionCode(questionCode);
        answer.setProfessorId(professorId);
        answer.setContent(XSSUtils.sanitizeInput(content.trim())); // XSS 정제
        answer.setAnswerTime(LocalDateTime.now());

        return answerRepository.save(answer);
    }

    // 교수의 강의 목록 조회
    public List<Course> getProfessorCourses(String professorId) {
//        return courseRepository.findByProfessorId(professorId);
        return courseRepository.findByProfessorOrSubProfessor(professorId);
    }

    // 강의 코드로 부교수 추가
    @Transactional
    public void addSubProfessorToCourse(String courseCode, String professorId) {
        Course course = courseRepository.findByCourseCode(courseCode);

        if (course == null) {
            throw new RuntimeException("존재하지 않는 강의 코드입니다.");
        }

        // 이미 메인 교수인 경우
        if (course.getProfessorId().equals(professorId)) {
            throw new RuntimeException("이미 해당 강의의 메인 교수입니다.");
        }

        // 이미 부교수로 등록된 경우
        if (course.getSubProfessors() != null && course.getSubProfessors().contains(professorId)) {
            throw new RuntimeException("이미 해당 강의의 부교수로 등록되어 있습니다.");
        }

        // 부교수 목록이 null이면 초기화
        if (course.getSubProfessors() == null) {
            course.setSubProfessors(new ArrayList<>());
        }

        // 부교수 추가
        course.getSubProfessors().add(professorId);
        courseRepository.save(course);
    }

    // 부교수 제거 (자신만 제거 가능)
    @Transactional
    public void removeSubProfessorFromCourse(String courseCode, String professorId) {
        Course course = courseRepository.findByCourseCode(courseCode);

        if (course == null) {
            throw new RuntimeException("존재하지 않는 강의입니다.");
        }

        if (course.getSubProfessors() == null || !course.getSubProfessors().contains(professorId)) {
            throw new RuntimeException("해당 강의의 부교수가 아닙니다.");
        }

        course.getSubProfessors().remove(professorId);
        courseRepository.save(course);
    }

    // 강의 상세 정보 조회
    public Course getCourseDetails(String courseCode) {
        return courseRepository.findById(courseCode)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
    }

    // 특정 강의의 수강생 목록 조회
    public List<Student> getCourseStudents(String courseCode) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseCode(courseCode);
        List<Student> students = new java.util.ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Student student = studentRepository.findByStudentId(enrollment.getStudentId());
            if (student != null) {
                students.add(student);
            }
        }

        return students;
    }

    // 학생의 특정 강의에서의 성적 정보 조회
    public List<Submission> getStudentSubmissionsInCourse(String studentId, String courseCode) {
        List<Assignment> courseAssignments = assignmentRepository.findByCourseCode(courseCode);
        List<Submission> submissions = new java.util.ArrayList<>();

        for (Assignment assignment : courseAssignments) {
            submissionRepository.findByAssignmentCodeAndStudentId(
                    assignment.getAssignmentCode(), studentId
            ).ifPresent(submissions::add);
        }

        return submissions;
    }

    // ProfessorService.java에 추가할 메서드들

    // 특정 강의의 과제 목록 조회
    public List<Assignment> getCourseAssignments(String courseCode) {
        return assignmentRepository.findByCourseCode(courseCode);
    }

    // 강의 삭제 (과제, 제출물, 질문, 답변, 수강신청 모두 삭제)
    @Transactional
    public void deleteCourse(String courseCode) {
        // 1. 강의의 모든 과제 조회
        List<Assignment> assignments = assignmentRepository.findByCourseCode(courseCode);

        for (Assignment assignment : assignments) {
            // 2. 각 과제의 모든 제출물 삭제
            List<Submission> submissions = submissionRepository.findByAssignmentCode(assignment.getAssignmentCode());
            if (!submissions.isEmpty()) {
                submissionRepository.deleteAll(submissions);
            }

            // 3. 각 과제의 모든 질문과 답변 삭제
            List<Question> questions = questionRepository.findByAssignmentCode(assignment.getAssignmentCode());
            for (Question question : questions) {
                List<Answer> answers = answerRepository.findByQuestionCode(question.getQuestionCode());
                if (!answers.isEmpty()) {
                    answerRepository.deleteAll(answers);
                }
            }
            if (!questions.isEmpty()) {
                questionRepository.deleteAll(questions);
            }
        }

        // 4. 모든 과제 삭제
        if (!assignments.isEmpty()) {
            assignmentRepository.deleteAll(assignments);
        }

        // 5. 수강신청 정보 삭제
        List<Enrollment> enrollments = enrollmentRepository.findByCourseCode(courseCode);
        if (!enrollments.isEmpty()) {
            enrollmentRepository.deleteAll(enrollments);
        }

        // 6. 강의 삭제
        courseRepository.deleteById(courseCode);
    }

    // 수강생을 강의에서 제외
    @Transactional
    public void removeStudentFromCourse(String studentId, String courseCode) {
        // 1. 수강신청 정보 확인
        if (!enrollmentRepository.existsByStudentIdAndCourseCode(studentId, courseCode)) {
            throw new RuntimeException("해당 학생은 이 강의를 수강하지 않습니다.");
        }

        // 2. 해당 학생의 이 강의 관련 제출물들 먼저 삭제
        List<Assignment> assignments = assignmentRepository.findByCourseCode(courseCode);
        for (Assignment assignment : assignments) {
            submissionRepository.findByAssignmentCodeAndStudentId(assignment.getAssignmentCode(), studentId)
                    .ifPresent(submission -> {
                        submissionRepository.delete(submission);
                    });

            // 3. 해당 학생의 질문과 답변도 삭제
            List<Question> questions = questionRepository.findByAssignmentCodeAndStudentId(assignment.getAssignmentCode(), studentId);
            for (Question question : questions) {
                List<Answer> answers = answerRepository.findByQuestionCode(question.getQuestionCode());
                if (!answers.isEmpty()) {
                    answerRepository.deleteAll(answers);
                }
            }
            if (!questions.isEmpty()) {
                questionRepository.deleteAll(questions);
            }
        }

        // 4. 수강신청 정보 삭제
        enrollmentRepository.deleteByStudentIdAndCourseCode(studentId, courseCode);
    }

    // 과제 정보 수정 (XSS 방어 추가)
    @Transactional
    public void updateAssignment(int assignmentCode, String courseCode, String title, String content, String dueDate, String dueTime) {
        Assignment assignment = assignmentRepository.findById(assignmentCode)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));

        // 입력값 유효성 검사
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("과제 제목을 입력해주세요.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("과제 내용을 입력해주세요.");
        }

        if (dueDate == null || dueDate.trim().isEmpty() || dueTime == null || dueTime.trim().isEmpty()) {
            throw new RuntimeException("마감일과 마감시간을 모두 입력해주세요.");
        }

        // XSS 검증 추가
        XSSUtils.validateInput(title, "과제 제목");
        XSSUtils.validateInput(content, "과제 내용");

        // 길이 제한
        if (title.length() > 200) {
            throw new RuntimeException("과제 제목이 너무 깁니다. (최대 200자)");
        }

        if (content.length() > 5000) {
            throw new RuntimeException("과제 내용이 너무 깁니다. (최대 5,000자)");
        }

        // 강의 존재 여부 확인
        Course course = courseRepository.findById(courseCode)
                .orElseThrow(() -> new RuntimeException("선택한 강의를 찾을 수 없습니다."));

        try {
            // 날짜와 시간을 합쳐서 LocalDateTime으로 변환
            LocalDate date = LocalDate.parse(dueDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime time = LocalTime.parse(dueTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime dueDateTime = LocalDateTime.of(date, time);

            // 마감일이 현재 시간보다 이전인지 확인 (선택적)
            if (dueDateTime.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("마감일은 현재 시간보다 이후여야 합니다.");
            }

            // 과제 정보 업데이트 (XSS 정제된 값으로)
            assignment.setCourseCode(courseCode);
            assignment.setCourse(course);
            assignment.setTitle(XSSUtils.sanitizeInput(title.trim()));
            assignment.setContent(XSSUtils.sanitizeInput(content.trim()));
            assignment.setDueDate(dueDateTime);

            assignmentRepository.save(assignment);

        } catch (DateTimeParseException e) {
            throw new RuntimeException("날짜 또는 시간 형식이 올바르지 않습니다.");
        }
    }

    // 과제 삭제 (제출물, 질문, 답변 모두 삭제)
    @Transactional
    public void deleteAssignment(int assignmentCode) {
        Assignment assignment = assignmentRepository.findById(assignmentCode)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));

        // 1. 모든 제출물 삭제
        List<Submission> submissions = submissionRepository.findByAssignmentCode(assignmentCode);
        if (!submissions.isEmpty()) {
            submissionRepository.deleteAll(submissions);
        }

        // 2. 모든 질문과 답변 삭제
        List<Question> questions = questionRepository.findByAssignmentCode(assignmentCode);
        for (Question question : questions) {
            List<Answer> answers = answerRepository.findByQuestionCode(question.getQuestionCode());
            if (!answers.isEmpty()) {
                answerRepository.deleteAll(answers);
            }
        }
        if (!questions.isEmpty()) {
            questionRepository.deleteAll(questions);
        }

        // 3. 과제 삭제
        assignmentRepository.delete(assignment);
    }


}