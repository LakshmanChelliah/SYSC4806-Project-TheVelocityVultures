package vv.pms.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vv.pms.allocation.AllocationService;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Program;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.report.ReportService;
import vv.pms.report.ReportSubmission;
import vv.pms.report.SystemConfigService;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportServiceIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private StudentService studentService;

    private Professor professor;
    private Project project;
    private Student student;

    @BeforeEach
    void setUp() {
        // Set deadline to future to allow submissions
        systemConfigService.setReportDeadline(LocalDateTime.now().plusDays(30));
        
        // Create test data
        professor = professorService.addProfessor("Report Prof", "report.prof@university.edu");
        project = projectService.addProject("Report Test Project", "A project for testing reports", 
                java.util.Set.of(Program.SOFTWARE_ENGINEERING), 5, professor.getId());
        student = studentService.addStudent("Report Student", "REPS001", "reps@university.edu", Program.SOFTWARE_ENGINEERING);
    }

    /**
     * Helper method to allocate student to project.
     * Note: Professor is already assigned when project is created via addProject().
     * This method only assigns the student using assignStudentToProject.
     */
    private void allocateStudentToProject(Project proj, Student stud) {
        allocationService.assignStudentToProject(proj.getId(), stud.getId());
    }

    @Test
    void canStudentSubmit_notAssigned_returnsFalse() {
        // Student not assigned to any project
        assertFalse(reportService.canStudentSubmit(student.getId()));
    }

    @Test
    void canStudentSubmit_assignedAndBeforeDeadline_returnsTrue() {
        // Assign student to project (professor already assigned via addProject)
        allocateStudentToProject(project, student);
        
        assertTrue(reportService.canStudentSubmit(student.getId()));
    }

    @Test
    void canStudentSubmit_deadlinePassed_returnsFalse() {
        // Assign student to project
        allocateStudentToProject(project, student);
        
        // Set deadline to past
        systemConfigService.setReportDeadline(LocalDateTime.now().minusDays(1));
        
        assertFalse(reportService.canStudentSubmit(student.getId()));
    }

    @Test
    void getSubmissionStatus_notAssigned() {
        String status = reportService.getSubmissionStatus(student.getId());
        assertEquals("Not assigned to a project", status);
    }

    @Test
    void getSubmissionStatus_assignedAndReady() {
        allocateStudentToProject(project, student);
        
        String status = reportService.getSubmissionStatus(student.getId());
        assertEquals("Ready to submit", status);
    }

    @Test
    void getSubmissionStatus_deadlinePassed() {
        allocateStudentToProject(project, student);
        
        systemConfigService.setReportDeadline(LocalDateTime.now().minusDays(1));
        
        String status = reportService.getSubmissionStatus(student.getId());
        assertEquals("Deadline has passed", status);
    }

    @Test
    void submitReport_success() {
        // Setup allocation
        allocateStudentToProject(project, student);
        
        MultipartFile file = new MockMultipartFile(
                "file",
                "test-report.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );
        
        reportService.submitReport(project.getId(), student.getId(), "test-report.pdf", file);
        
        Optional<ReportSubmission> submission = reportService.getReportByProject(project.getId());
        assertTrue(submission.isPresent());
        assertEquals("test-report.pdf", submission.get().getFilename());
        assertEquals(project.getId(), submission.get().getProjectId());
        assertEquals(student.getId(), submission.get().getSubmittedByStudentId());
        assertNotNull(submission.get().getSubmittedAt());
    }

    @Test
    void submitReport_afterDeadline_throws() {
        allocateStudentToProject(project, student);
        
        systemConfigService.setReportDeadline(LocalDateTime.now().minusDays(1));
        
        MultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "content".getBytes());
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                reportService.submitReport(project.getId(), student.getId(), "report.pdf", file)
        );
        assertEquals("The submission deadline has passed.", ex.getMessage());
    }

    @Test
    void submitReport_studentNotAssigned_throws() {
        // Create another student who is not assigned
        Student unassignedStudent = studentService.addStudent("Unassigned", "UNAS001", "unas@university.edu", Program.SOFTWARE_ENGINEERING);
        
        MultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "content".getBytes());
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                reportService.submitReport(project.getId(), unassignedStudent.getId(), "report.pdf", file)
        );
        assertEquals("Student is not assigned to this project.", ex.getMessage());
    }

    @Test
    void submitReport_emptyFile_throws() {
        allocateStudentToProject(project, student);
        
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reportService.submitReport(project.getId(), student.getId(), "empty.pdf", emptyFile)
        );
        assertEquals("Cannot submit an empty file.", ex.getMessage());
    }

    @Test
    void submitReport_notPdf_throws() {
        allocateStudentToProject(project, student);
        
        MultipartFile txtFile = new MockMultipartFile("file", "report.txt", "text/plain", "content".getBytes());
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reportService.submitReport(project.getId(), student.getId(), "report.txt", txtFile)
        );
        assertEquals("Only PDF files are allowed.", ex.getMessage());
    }

    @Test
    void submitReport_overwritesPreviousSubmission() {
        allocateStudentToProject(project, student);
        
        MultipartFile file1 = new MockMultipartFile("file", "report-v1.pdf", "application/pdf", "version 1".getBytes());
        reportService.submitReport(project.getId(), student.getId(), "report-v1.pdf", file1);
        
        MultipartFile file2 = new MockMultipartFile("file", "report-v2.pdf", "application/pdf", "version 2".getBytes());
        reportService.submitReport(project.getId(), student.getId(), "report-v2.pdf", file2);
        
        Optional<ReportSubmission> submission = reportService.getReportByProject(project.getId());
        assertTrue(submission.isPresent());
        assertEquals("report-v2.pdf", submission.get().getFilename());
    }

    @Test
    void getReportByProject_noSubmission() {
        Optional<ReportSubmission> submission = reportService.getReportByProject(project.getId());
        assertTrue(submission.isEmpty());
    }

    @Test
    void getReportByProject_success() {
        allocateStudentToProject(project, student);
        
        MultipartFile file = new MockMultipartFile("file", "my-report.pdf", "application/pdf", "content".getBytes());
        reportService.submitReport(project.getId(), student.getId(), "my-report.pdf", file);
        
        Optional<ReportSubmission> submission = reportService.getReportByProject(project.getId());
        
        assertTrue(submission.isPresent());
        assertEquals("my-report.pdf", submission.get().getFilename());
        assertNotNull(submission.get().getFilePath());
        assertTrue(submission.get().getFilePath().contains("my-report.pdf"));
    }

    @Test
    void submitReport_multipleProjects() {
        // Create second project and student
        Project project2 = projectService.addProject("Second Project", "Another project", 
                java.util.Set.of(Program.SOFTWARE_ENGINEERING), 5, professor.getId());
        Student student2 = studentService.addStudent("Student 2", "STU2001", "stu2@university.edu", Program.SOFTWARE_ENGINEERING);
        
        // Allocate students (professors already assigned via addProject)
        allocateStudentToProject(project, student);
        allocateStudentToProject(project2, student2);
        
        // Submit reports for both
        MultipartFile file1 = new MockMultipartFile("file", "report1.pdf", "application/pdf", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "report2.pdf", "application/pdf", "content2".getBytes());
        
        reportService.submitReport(project.getId(), student.getId(), "report1.pdf", file1);
        reportService.submitReport(project2.getId(), student2.getId(), "report2.pdf", file2);
        
        // Verify both submissions
        Optional<ReportSubmission> sub1 = reportService.getReportByProject(project.getId());
        Optional<ReportSubmission> sub2 = reportService.getReportByProject(project2.getId());
        
        assertTrue(sub1.isPresent());
        assertTrue(sub2.isPresent());
        assertEquals("report1.pdf", sub1.get().getFilename());
        assertEquals("report2.pdf", sub2.get().getFilename());
    }

    @Test
    void submitReport_pdfExtensionCaseInsensitive() {
        allocateStudentToProject(project, student);
        
        MultipartFile file = new MockMultipartFile("file", "report.PDF", "application/pdf", "content".getBytes());
        
        // Should not throw - PDF extension is case-insensitive
        assertDoesNotThrow(() ->
                reportService.submitReport(project.getId(), student.getId(), "report.PDF", file)
        );
    }

    @Test
    void submitReport_differentStudentOnSameProject_throws() {
        // Assign first student to project
        allocateStudentToProject(project, student);
        
        // Create another student who is NOT assigned to this project
        Student otherStudent = studentService.addStudent("Other Student", "OTHER001", "other@university.edu", Program.SOFTWARE_ENGINEERING);
        
        MultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "content".getBytes());
        
        // Other student should not be able to submit to this project
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                reportService.submitReport(project.getId(), otherStudent.getId(), "report.pdf", file)
        );
        assertEquals("Student is not assigned to this project.", ex.getMessage());
    }

    @Test
    void submitReport_multipleStudentsOnSameProject() {
        // Create second student
        Student student2 = studentService.addStudent("Student 2", "STUD2001", "stud2@university.edu", Program.SOFTWARE_ENGINEERING);
        
        // Project already has professor assigned via addProject, just assign students
        allocationService.assignStudentToProject(project.getId(), student.getId());
        allocationService.assignStudentToProject(project.getId(), student2.getId());
        
        // First student submits
        MultipartFile file1 = new MockMultipartFile("file", "report-s1.pdf", "application/pdf", "student 1 content".getBytes());
        reportService.submitReport(project.getId(), student.getId(), "report-s1.pdf", file1);
        
        Optional<ReportSubmission> sub1 = reportService.getReportByProject(project.getId());
        assertTrue(sub1.isPresent());
        assertEquals(student.getId(), sub1.get().getSubmittedByStudentId());
        
        // Second student overwrites
        MultipartFile file2 = new MockMultipartFile("file", "report-s2.pdf", "application/pdf", "student 2 content".getBytes());
        reportService.submitReport(project.getId(), student2.getId(), "report-s2.pdf", file2);
        
        Optional<ReportSubmission> sub2 = reportService.getReportByProject(project.getId());
        assertTrue(sub2.isPresent());
        assertEquals("report-s2.pdf", sub2.get().getFilename());
        assertEquals(student2.getId(), sub2.get().getSubmittedByStudentId());
    }

    @Test
    void canStudentSubmit_withDeadlineSet_returnsTrue() {
        allocateStudentToProject(project, student);
        
        // With future deadline already set in setUp, student should be able to submit
        assertTrue(reportService.canStudentSubmit(student.getId()));
    }
}
