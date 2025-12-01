package vv.pms.coordinator.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.coordinator.Coordinator;

import java.util.Optional;

public interface CoordinatorRepository extends JpaRepository<Coordinator, Long> {
    Optional<Coordinator> findByEmail(String email);
}
