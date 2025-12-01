package vv.pms.availability.internal;

import org.springframework.data.repository.CrudRepository;
import vv.pms.availability.Availability;

import java.util.Optional;

public interface AvailabilityRepository extends CrudRepository<Availability, Long> {
    Optional<Availability> findByUserIdAndUserType(Long userId, String userType);
}