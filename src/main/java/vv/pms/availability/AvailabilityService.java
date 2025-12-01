package vv.pms.availability;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.availability.internal.AvailabilityRepository;

@Service
@Transactional
public class AvailabilityService {

    private final AvailabilityRepository repository;

    public AvailabilityService(AvailabilityRepository repository) {
        this.repository = repository;
    }

    public Availability getAvailability(Long userId, String userType) {
        return repository.findByUserIdAndUserType(userId, userType)
                .orElseGet(() -> createDefault(userId, userType));
    }

    public void updateAvailability(Long userId, String userType, Boolean[][] timeslots) {
        Availability availability = getAvailability(userId, userType);
        availability.setTimeslots(timeslots);
        repository.save(availability);
    }

    private Availability createDefault(Long userId, String userType) {
        Boolean[][] matrix = new Boolean[5][32];
        for(int i=0; i<5; i++){
            for(int j=0; j<32; j++){
                matrix[i][j] = false;
            }
        }

        Availability newAvail = new Availability(userId, userType, matrix);
        return repository.save(newAvail);
    }
}