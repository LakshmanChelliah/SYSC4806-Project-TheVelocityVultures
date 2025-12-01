package vv.pms.coordinator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.coordinator.internal.CoordinatorRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CoordinatorService {

    private final CoordinatorRepository repository;

    public CoordinatorService(CoordinatorRepository repository) {
        this.repository = repository;
    }

    public Coordinator addCoordinator(String name, String email) {
        if (repository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Coordinator with email " + email + " already exists.");
        }
        Coordinator c = new Coordinator(name, email);
        return repository.save(c);
    }

    @Transactional(readOnly = true)
    public Optional<Coordinator> findByEmail(String email) { return repository.findByEmail(email); }

    @Transactional(readOnly = true)
    public Optional<Coordinator> findById(Long id) { return repository.findById(id); }

    @Transactional(readOnly = true)
    public List<Coordinator> findAll() { return repository.findAll(); }

    public void deleteCoordinator(Long id) {
        if (!repository.existsById(id)) {
            throw new CoordinatorNotFoundException("Coordinator ID " + id + " not found.");
        }
        repository.deleteById(id);
    }

    public void updateCoordinator(Long id, String name, String email) {
        Coordinator c = repository.findById(id)
                .orElseThrow(() -> new CoordinatorNotFoundException("Coordinator ID " + id + " not found."));
        if (!c.getEmail().equals(email) && repository.findByEmail(email).isPresent()) {
            throw new CoordinatorAlreadyExistsException("Coordinator email " + email + " already exists.");
        }
        c.setName(name);
        c.setEmail(email);
        repository.save(c);
    }
}

class CoordinatorNotFoundException extends RuntimeException {
    public CoordinatorNotFoundException(String message) { super(message); }
}

class CoordinatorAlreadyExistsException extends RuntimeException {
    public CoordinatorAlreadyExistsException(String message) { super(message); }
}
