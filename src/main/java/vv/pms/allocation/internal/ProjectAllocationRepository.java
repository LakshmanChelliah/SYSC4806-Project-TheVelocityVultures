package vv.pms.allocation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.allocation.ProjectAllocation;
import java.util.Optional;
import java.util.Set;
import java.util.List;

public interface ProjectAllocationRepository extends JpaRepository<ProjectAllocation, Long> {

    // Custom finder to check if a project is already allocated
    Optional<ProjectAllocation> findByProjectId(Long projectId);

    // Finds all allocations where the projectId is in the given set.
    List<ProjectAllocation> findByProjectIdIn(Set<Long> projectIds);

    // Finds all allocations for a specific professor
    List<ProjectAllocation> findByProfessorId(Long professorId);
}
