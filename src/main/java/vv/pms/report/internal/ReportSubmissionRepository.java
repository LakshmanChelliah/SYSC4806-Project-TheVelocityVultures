package vv.pms.report.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vv.pms.report.ReportSubmission;

import java.util.Optional;

@Repository
public interface ReportSubmissionRepository extends JpaRepository<ReportSubmission, Long> {
    Optional<ReportSubmission> findByProjectId(Long projectId);
}
