package vv.pms.report;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.report.internal.ReportSubmissionRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportSubmissionRepository repository;
    @Mock
    private SystemConfigService systemConfigService;
    @Mock
    private AllocationService allocationService;

    @InjectMocks
    private ReportService reportService;

    private final Long projectId = 1L;
    private final Long studentId = 100L;

    @AfterEach
    void cleanup() {
        // Best-effort cleanup of files created during tests
        try {
            Files.walk(Paths.get("uploads/reports"))
                .filter(p -> p.getFileName().toString().startsWith(projectId + "_"))
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException e) {}
                });
        } catch (IOException e) {
            // ignore
        }
    }

    @Test
    void submitReport_throwsWhenDeadlinePassed() {
        when(systemConfigService.isBeforeDeadline()).thenReturn(false);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        assertThrows(IllegalStateException.class, () -> 
            reportService.submitReport(projectId, studentId, "test.pdf", file)
        );
    }

    @Test
    void submitReport_throwsWhenStudentNotAssigned() {
        when(systemConfigService.isBeforeDeadline()).thenReturn(true);
        when(allocationService.findAllocationByProjectId(projectId)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        assertThrows(IllegalStateException.class, () -> 
            reportService.submitReport(projectId, studentId, "test.pdf", file)
        );
    }

    @Test
    void submitReport_throwsWhenFileEmpty() {
        when(systemConfigService.isBeforeDeadline()).thenReturn(true);
        ProjectAllocation allocation = new ProjectAllocation(projectId, 10L);
        allocation.addStudent(studentId);
        when(allocationService.findAllocationByProjectId(projectId)).thenReturn(Optional.of(allocation));
        
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> 
            reportService.submitReport(projectId, studentId, "test.pdf", file)
        );
    }

    @Test
    void submitReport_throwsWhenNotPdf() {
        when(systemConfigService.isBeforeDeadline()).thenReturn(true);
        ProjectAllocation allocation = new ProjectAllocation(projectId, 10L);
        allocation.addStudent(studentId);
        when(allocationService.findAllocationByProjectId(projectId)).thenReturn(Optional.of(allocation));
        
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        assertThrows(IllegalArgumentException.class, () -> 
            reportService.submitReport(projectId, studentId, "test.txt", file)
        );
    }

    @Test
    void submitReport_success() throws IOException {
        when(systemConfigService.isBeforeDeadline()).thenReturn(true);
        ProjectAllocation allocation = new ProjectAllocation(projectId, 10L);
        allocation.addStudent(studentId);
        when(allocationService.findAllocationByProjectId(projectId)).thenReturn(Optional.of(allocation));
        when(repository.findByProjectId(projectId)).thenReturn(Optional.empty());
        
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        reportService.submitReport(projectId, studentId, "test.pdf", file);

        verify(repository).save(any(ReportSubmission.class));
    }
    
    @Test
    void canStudentSubmit_checksDeadlineAndAllocation() {
        when(systemConfigService.isBeforeDeadline()).thenReturn(true);
        when(allocationService.findAllocationByStudentId(studentId)).thenReturn(Optional.of(new ProjectAllocation(projectId, 10L)));
        
        assertTrue(reportService.canStudentSubmit(studentId));
    }
    
    @Test
    void getSubmissionStatus_returnsReady() {
        when(systemConfigService.isBeforeDeadline()).thenReturn(true);
        when(allocationService.findAllocationByStudentId(studentId)).thenReturn(Optional.of(new ProjectAllocation(projectId, 10L)));
        
        assertEquals("Ready to submit", reportService.getSubmissionStatus(studentId));
    }
}
