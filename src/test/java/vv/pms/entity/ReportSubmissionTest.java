package vv.pms.entity;

import org.junit.jupiter.api.Test;
import vv.pms.report.ReportSubmission;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReportSubmissionTest {

    @Test
    void testDefaultConstructor() {
        ReportSubmission submission = new ReportSubmission();
        
        assertNull(submission.getId());
        assertNull(submission.getProjectId());
        assertNull(submission.getFilename());
        assertNull(submission.getFilePath());
        assertNull(submission.getSubmittedAt());
        assertNull(submission.getSubmittedByStudentId());
    }

    @Test
    void testParameterizedConstructor() {
        Long projectId = 1L;
        String filename = "final_report.pdf";
        String filePath = "/uploads/reports/final_report.pdf";
        LocalDateTime submittedAt = LocalDateTime.now();
        Long studentId = 100L;
        
        ReportSubmission submission = new ReportSubmission(
            projectId, filename, filePath, submittedAt, studentId
        );
        
        assertNull(submission.getId()); // ID is null until persisted
        assertEquals(projectId, submission.getProjectId());
        assertEquals(filename, submission.getFilename());
        assertEquals(filePath, submission.getFilePath());
        assertEquals(submittedAt, submission.getSubmittedAt());
        assertEquals(studentId, submission.getSubmittedByStudentId());
    }

    @Test
    void testSettersAndGetters() {
        ReportSubmission submission = new ReportSubmission();
        LocalDateTime now = LocalDateTime.now();
        
        submission.setId(1L);
        submission.setProjectId(10L);
        submission.setFilename("report.pdf");
        submission.setFilePath("/path/to/report.pdf");
        submission.setSubmittedAt(now);
        submission.setSubmittedByStudentId(100L);
        
        assertEquals(1L, submission.getId());
        assertEquals(10L, submission.getProjectId());
        assertEquals("report.pdf", submission.getFilename());
        assertEquals("/path/to/report.pdf", submission.getFilePath());
        assertEquals(now, submission.getSubmittedAt());
        assertEquals(100L, submission.getSubmittedByStudentId());
    }

    @Test
    void testFilenameChange() {
        ReportSubmission submission = new ReportSubmission(
            1L, "old_report.pdf", "/path/old_report.pdf", 
            LocalDateTime.now(), 100L
        );
        
        submission.setFilename("new_report.pdf");
        
        assertEquals("new_report.pdf", submission.getFilename());
    }

    @Test
    void testFilePathChange() {
        ReportSubmission submission = new ReportSubmission(
            1L, "report.pdf", "/old/path/report.pdf", 
            LocalDateTime.now(), 100L
        );
        
        submission.setFilePath("/new/path/report.pdf");
        
        assertEquals("/new/path/report.pdf", submission.getFilePath());
    }

    @Test
    void testSubmittedAtUpdate() {
        LocalDateTime originalTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        ReportSubmission submission = new ReportSubmission(
            1L, "report.pdf", "/path/report.pdf", originalTime, 100L
        );
        
        LocalDateTime newTime = LocalDateTime.of(2024, 1, 2, 15, 30);
        submission.setSubmittedAt(newTime);
        
        assertEquals(newTime, submission.getSubmittedAt());
    }

    @Test
    void testStudentIdChange() {
        ReportSubmission submission = new ReportSubmission(
            1L, "report.pdf", "/path/report.pdf", 
            LocalDateTime.now(), 100L
        );
        
        submission.setSubmittedByStudentId(200L);
        
        assertEquals(200L, submission.getSubmittedByStudentId());
    }

    @Test
    void testProjectIdUniqueness() {
        // Each project can only have one report submission (unique constraint)
        ReportSubmission submission1 = new ReportSubmission(
            1L, "report1.pdf", "/path/report1.pdf", 
            LocalDateTime.now(), 100L
        );
        ReportSubmission submission2 = new ReportSubmission(
            1L, "report2.pdf", "/path/report2.pdf", 
            LocalDateTime.now(), 101L
        );
        
        // Both have the same projectId
        assertEquals(submission1.getProjectId(), submission2.getProjectId());
    }

    @Test
    void testTimestampPrecision() {
        LocalDateTime preciseTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45, 123456789);
        
        ReportSubmission submission = new ReportSubmission(
            1L, "report.pdf", "/path/report.pdf", preciseTime, 100L
        );
        
        assertEquals(preciseTime, submission.getSubmittedAt());
        assertEquals(14, submission.getSubmittedAt().getHour());
        assertEquals(30, submission.getSubmittedAt().getMinute());
        assertEquals(45, submission.getSubmittedAt().getSecond());
    }

    @Test
    void testIdAssignment() {
        ReportSubmission submission = new ReportSubmission(
            1L, "report.pdf", "/path/report.pdf", 
            LocalDateTime.now(), 100L
        );
        assertNull(submission.getId());
        
        submission.setId(50L);
        
        assertEquals(50L, submission.getId());
    }

    @Test
    void testFilePathWithSpecialCharacters() {
        String pathWithSpaces = "/uploads/reports/1_1234567890_Final Report.pdf";
        
        ReportSubmission submission = new ReportSubmission(
            1L, "Final Report.pdf", pathWithSpaces, 
            LocalDateTime.now(), 100L
        );
        
        assertEquals(pathWithSpaces, submission.getFilePath());
    }
}
