package vv.pms.report;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_submissions")
public class ReportSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long projectId;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private Long submittedByStudentId;

    public ReportSubmission() {}

    public ReportSubmission(Long projectId, String filename, String filePath, LocalDateTime submittedAt, Long submittedByStudentId) {
        this.projectId = projectId;
        this.filename = filename;
        this.filePath = filePath;
        this.submittedAt = submittedAt;
        this.submittedByStudentId = submittedByStudentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Long getSubmittedByStudentId() {
        return submittedByStudentId;
    }

    public void setSubmittedByStudentId(Long submittedByStudentId) {
        this.submittedByStudentId = submittedByStudentId;
    }
}
