package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdministratorService {

    @Autowired
    private AdministratorRepository administratorRepository;

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

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    // 인증 관련 메서드들은 AuthenticationService로 이동되었으므로 제거

    // 승인 대기중인 학생 목록 조회
    public List<Student> getPendingStudents() {
        return studentRepository.findAll().stream()
                .filter(student -> !student.isAllowed())
                .toList();
    }

    // 승인 대기중인 교수 목록 조회
    public List<Professor> getPendingProfessors() {
        return professorRepository.findAll().stream()
                .filter(professor -> !professor.isAllowed())
                .toList();
    }

    // 승인된 학생 목록 조회
    public List<Student> getApprovedStudents() {
        return studentRepository.findAll().stream()
                .filter(Student::isAllowed)
                .toList();
    }

    // 승인된 교수 목록 조회
    public List<Professor> getApprovedProfessors() {
        return professorRepository.findAll().stream()
                .filter(Professor::isAllowed)
                .toList();
    }

    // 학생 승인
    public boolean approveStudent(String studentId) {
        try {
            Student student = studentRepository.findByStudentId(studentId);
            if (student != null && !student.isAllowed()) {
                student.setAllowed(true);
                studentRepository.save(student);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error approving student: " + e.getMessage());
            return false;
        }
    }

    // 교수 승인
    public boolean approveProfessor(String professorId) {
        try {
            Professor professor = professorRepository.findByProfessorId(professorId);
            if (professor != null && !professor.isAllowed()) {
                professor.setAllowed(true);
                professorRepository.save(professor);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error approving professor: " + e.getMessage());
            return false;
        }
    }

    // 학생 승인 취소 (이미 승인된 학생의 권한 회수)
    public boolean revokeStudent(String studentId) {
        try {
            Student student = studentRepository.findByStudentId(studentId);
            if (student != null && student.isAllowed()) {
                student.setAllowed(false);
                studentRepository.save(student);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error revoking student: " + e.getMessage());
            return false;
        }
    }

    // 교수 승인 취소 (이미 승인된 교수의 권한 회수)
    public boolean revokeProfessor(String professorId) {
        try {
            Professor professor = professorRepository.findByProfessorId(professorId);
            if (professor != null && professor.isAllowed()) {
                professor.setAllowed(false);
                professorRepository.save(professor);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error revoking professor: " + e.getMessage());
            return false;
        }
    }

    // 학생 삭제 (회원가입 거부)
    public boolean rejectStudent(String studentId) {
        try {
            Student student = studentRepository.findByStudentId(studentId);
            if (student != null && !student.isAllowed()) {
                studentRepository.delete(student);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error rejecting student: " + e.getMessage());
            return false;
        }
    }

    // 교수 삭제 (회원가입 거부)
    public boolean rejectProfessor(String professorId) {
        try {
            Professor professor = professorRepository.findByProfessorId(professorId);
            if (professor != null && !professor.isAllowed()) {
                professorRepository.delete(professor);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error rejecting professor: " + e.getMessage());
            return false;
        }
    }

    // 전체 통계 조회
    public AdminStatistics getStatistics() {
        List<Student> allStudents = studentRepository.findAll();
        List<Professor> allProfessors = professorRepository.findAll();

        int pendingStudents = (int) allStudents.stream().filter(s -> !s.isAllowed()).count();
        int approvedStudents = (int) allStudents.stream().filter(Student::isAllowed).count();
        int pendingProfessors = (int) allProfessors.stream().filter(p -> !p.isAllowed()).count();
        int approvedProfessors = (int) allProfessors.stream().filter(Professor::isAllowed).count();

        // 강의 및 과제 통계 추가
        int totalCourses = courseRepository.findAll().size();
        int totalAssignments = assignmentRepository.findAll().size();

        return new AdminStatistics(pendingStudents, approvedStudents, pendingProfessors, approvedProfessors, totalCourses, totalAssignments);
    }

    // === 강의 관리 기능 ===

    // 모든 강의 조회 (교수 정보와 수강생 수 포함)
    public List<Course> getAllCourses() {
        try {
            List<Course> courses = courseRepository.findAll();

            for (Course course : courses) {
                try {
                    // 수강생 수 계산
                    int studentCount = enrollmentRepository.findByCourseCode(course.getCourseCode()).size();
                    course.setStudentCount(studentCount);

                    // 과제 수 계산
                    int assignmentCount = assignmentRepository.findByCourseCode(course.getCourseCode()).size();
                    course.setAssignmentCount(assignmentCount);

                } catch (Exception e) {
                    course.setStudentCount(0);
                    course.setAssignmentCount(0);
                }
            }

            return courses;
        } catch (Exception e) {
            System.err.println("Error loading all courses: " + e.getMessage());
            return List.of();
        }
    }

    // 특정 강의 상세 정보 조회
    public Course getCourseDetails(String courseCode) {
        try {
            Course course = courseRepository.findByCourseCode(courseCode);
            if (course != null) {
                // 수강생 수와 과제 수 설정
                int studentCount = enrollmentRepository.findByCourseCode(courseCode).size();
                int assignmentCount = assignmentRepository.findByCourseCode(courseCode).size();
                course.setStudentCount(studentCount);
                course.setAssignmentCount(assignmentCount);
            }
            return course;
        } catch (Exception e) {
            System.err.println("Error loading course details: " + e.getMessage());
            return null;
        }
    }

    // 강의 삭제 (모든 관련 데이터 포함)
    @Transactional
    public boolean deleteCourse(String courseCode) {
        try {
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

            return true;
        } catch (Exception e) {
            System.err.println("Error deleting course: " + e.getMessage());
            return false;
        }
    }

    // === 과제 관리 기능 ===

    // 모든 과제 조회 (강의 정보와 제출 통계 포함)
    public List<Assignment> getAllAssignments() {
        try {
            List<Assignment> assignments = assignmentRepository.findAll();

            for (Assignment assignment : assignments) {
                try {
                    // 강의 정보 로드
                    Course course = courseRepository.findByCourseCode(assignment.getCourseCode());
                    assignment.setCourse(course);

                } catch (Exception e) {
                    System.err.println("Error loading course for assignment " + assignment.getAssignmentCode());
                }
            }

            return assignments;
        } catch (Exception e) {
            System.err.println("Error loading all assignments: " + e.getMessage());
            return List.of();
        }
    }

    // 특정 과제의 제출 통계 조회
    public AssignmentStatistics getAssignmentStatistics(int assignmentCode) {
        try {
            Assignment assignment = assignmentRepository.findById(assignmentCode).orElse(null);
            if (assignment == null) {
                return null;
            }

            // 해당 강의의 전체 수강생 수
            int totalStudents = enrollmentRepository.findByCourseCode(assignment.getCourseCode()).size();

            // 제출된 과제 수
            int submittedCount = submissionRepository.findByAssignmentCode(assignmentCode).size();

            // 미제출 과제 수
            int notSubmittedCount = totalStudents - submittedCount;

            // 질문 수
            int questionCount = questionRepository.findByAssignmentCode(assignmentCode).size();

            return new AssignmentStatistics(totalStudents, submittedCount, notSubmittedCount, questionCount);

        } catch (Exception e) {
            System.err.println("Error loading assignment statistics: " + e.getMessage());
            return null;
        }
    }

    // 과제 삭제 (모든 관련 데이터 포함)
    @Transactional
    public boolean deleteAssignment(int assignmentCode) {
        try {
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
            assignmentRepository.deleteById(assignmentCode);

            return true;
        } catch (Exception e) {
            System.err.println("Error deleting assignment: " + e.getMessage());
            return false;
        }
    }

    // 통계 데이터를 담을 내부 클래스 (수정됨)
    @Getter
    public static class AdminStatistics {
        private final int pendingStudents;
        private final int approvedStudents;
        private final int pendingProfessors;
        private final int approvedProfessors;
        private final int totalCourses;
        private final int totalAssignments;

        public AdminStatistics(int pendingStudents, int approvedStudents,
                               int pendingProfessors, int approvedProfessors,
                               int totalCourses, int totalAssignments) {
            this.pendingStudents = pendingStudents;
            this.approvedStudents = approvedStudents;
            this.pendingProfessors = pendingProfessors;
            this.approvedProfessors = approvedProfessors;
            this.totalCourses = totalCourses;
            this.totalAssignments = totalAssignments;
        }
    }

    // 과제 통계 데이터를 담을 내부 클래스
    @Getter
    public static class AssignmentStatistics {
        private final int totalStudents;
        private final int submittedCount;
        private final int notSubmittedCount;
        private final int questionCount;

        public AssignmentStatistics(int totalStudents, int submittedCount,
                                    int notSubmittedCount, int questionCount) {
            this.totalStudents = totalStudents;
            this.submittedCount = submittedCount;
            this.notSubmittedCount = notSubmittedCount;
            this.questionCount = questionCount;
        }
    }
}