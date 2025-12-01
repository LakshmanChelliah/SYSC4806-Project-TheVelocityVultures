package vv.pms.project;

import java.util.Optional;

public interface ProjectOwnershipGateway {
    // This method returns void to break the dependency cycle
    void assignProjectOwner(Long projectId, Long professorId);

    Optional<Long> findProjectOwnerId(Long projectId);
}