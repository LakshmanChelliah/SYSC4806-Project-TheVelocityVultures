package vv.pms.report;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.report.internal.ReportSubmissionRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ReportService {

    private final ReportSubmissionRepository repository;
    private final SystemConfigService systemConfigService;
    private final AllocationService allocationService;
    private final Path fileStorageLocation;

    public ReportService(ReportSubmissionRepository repository,
                         SystemConfigService systemConfigService,
                         AllocationService allocationService) {
        this.repository = repository;
        this.systemConfigService = systemConfigService;
        this.allocationService = allocationService;
        
        // Configurable path, could be injected via properties
        this.fileStorageLocation = Paths.get("uploads/reports").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public void submitReport(Long projectId, Long studentId, String filename, MultipartFile file) {
        // 1. Check Deadline
        if (!systemConfigService.isBeforeDeadline()) {
            throw new IllegalStateException("The submission deadline has passed.");
        }

        // 2. Check Assignment
        Optional<ProjectAllocation> allocationOpt = allocationService.findAllocationByProjectId(projectId);
        if (allocationOpt.isEmpty() || !allocationOpt.get().getAssignedStudentIds().contains(studentId)) {
            throw new IllegalStateException("Student is not assigned to this project.");
        }

        // 3. Validate File
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot submit an empty file.");
        }
        if (!filename.toLowerCase().endsWith(".pdf")) {
             throw new IllegalArgumentException("Only PDF files are allowed.");
        }

        try {
            // 4. Save File
            // Ensure directory exists
            if (!Files.exists(this.fileStorageLocation)) {
                Files.createDirectories(this.fileStorageLocation);
            }

            // Normalize filename
            String safeFilename = projectId + "_" + System.currentTimeMillis() + "_" + filename;
            Path targetLocation = this.fileStorageLocation.resolve(safeFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 5. Update Database
            // Check if submission already exists for this project
            Optional<ReportSubmission> existingSubmission = repository.findByProjectId(projectId);
            ReportSubmission submission;
            if (existingSubmission.isPresent()) {
                submission = existingSubmission.get();
                // Delete old file if needed, or just overwrite reference
                try {
                    Files.deleteIfExists(Paths.get(submission.getFilePath()));
                } catch (IOException e) {
                    // Log warning but continue
                    System.err.println("Could not delete old file: " + submission.getFilePath());
                }
                submission.setFilename(filename);
                submission.setFilePath(targetLocation.toString());
                submission.setSubmittedAt(LocalDateTime.now());
                submission.setSubmittedByStudentId(studentId);
            } else {
                submission = new ReportSubmission(projectId, filename, targetLocation.toString(), LocalDateTime.now(), studentId);
            }
            repository.save(submission);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + filename + ". Please try again!", ex);
        }
    }

    @Transactional(readOnly = true)
    public Optional<ReportSubmission> getReportByProject(Long projectId) {
        return repository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public boolean canStudentSubmit(Long studentId) {
        if (!systemConfigService.isBeforeDeadline()) {
            return false;
        }
        return allocationService.findAllocationByStudentId(studentId).isPresent();
    }

    @Transactional(readOnly = true)
    public String getSubmissionStatus(Long studentId) {
        Optional<ProjectAllocation> allocation = allocationService.findAllocationByStudentId(studentId);
        if (allocation.isEmpty()) {
            return "Not assigned to a project";
        }
        
        if (!systemConfigService.isBeforeDeadline()) {
            return "Deadline has passed";
        }

        return "Ready to submit";
    }
}
