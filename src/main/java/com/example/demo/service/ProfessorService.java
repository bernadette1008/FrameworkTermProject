package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        return assignmentRepository.findByProfessorId(professorId);
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

    // 채점 처리
    public void gradeSubmission(int submissionCode, Integer score, String feedback) {
        Submission submission = getSubmissionDetails(submissionCode);

        if (score < 0 || score > 100) {
            throw new RuntimeException("점수는 0-100 사이의 값이어야 합니다.");
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

    // 질문 상세 정보 조회
    public Question getQuestionDetails(int questionCode) {
        Question question = questionRepository.findByQuestionCode(questionCode)
                .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));

        // 답변 목록도 함께 로드
        List<Answer> answers = answerRepository.findByQuestionCodeOrderByAnswerTimeAsc(questionCode);
        question.setAnswers(answers);

        return question;
    }

    // 질문 답변 등록
    public Answer answerQuestion(int questionCode, String professorId, String content) {
        Question question = getQuestionDetails(questionCode);
        Professor professor = professorRepository.findByProfessorId(professorId);

        if (professor == null) {
            throw new RuntimeException("교수를 찾을 수 없습니다.");
        }

        Answer answer = new Answer();
        answer.setQuestionCode(questionCode);
        answer.setProfessorId(professorId);
        answer.setContent(content);
        answer.setAnswerTime(LocalDateTime.now());

        return answerRepository.save(answer);
    }

    // 교수의 강의 목록 조회
    public List<Course> getProfessorCourses(String professorId) {
        return courseRepository.findByProfessorId(professorId);
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
}