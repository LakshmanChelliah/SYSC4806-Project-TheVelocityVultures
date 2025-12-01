package vv.pms.professor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.professor.internal.ProfessorRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProfessorService {

    private final ProfessorRepository repository;

    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

    /**
     * Adds a new professor to the system.
     */
    public Professor addProfessor(String name, String email) {
        if (repository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Professor with email " + email + " already exists.");
        }
        Professor newProfessor = new Professor(name, email);
        return repository.save(newProfessor);
    }

    /**
     * Find a professor by email. Public API used by other modules (e.g., auth).
     */
    @Transactional(readOnly = true)
    public Optional<Professor> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    /**
     * Retrieves a professor by their ID.
     */
    @Transactional(readOnly = true)
    public Optional<Professor> findProfessorById(Long id) {
        return repository.findById(id);
    }

    /**
     * Retrieves all professors.
     */
    @Transactional(readOnly = true)
    public List<Professor> findAllProfessors() {
        return repository.findAll();
    }

    /**
     * Deletes a professor by ID.
     * TODO: Checks before deletion? (are they assigned to any projects?)
     */
    public void deleteProfessor(Long id) {
        if (!repository.existsById(id)) {
            throw new ProfessorNotFoundException("Professor with ID " + id + " not found.");
        }
        repository.deleteById(id);
    }

    public void modifyProfessor(Long id, String name, String email) {
        Professor professor = repository.findById(id)
                .orElseThrow(() -> new ProfessorNotFoundException("Professor with ID " + id + " not found."));

        // Check for email uniqueness if it's being changed
        if (!professor.getEmail().equals(email) && repository.findByEmail(email).isPresent()) {
            throw new ProfessorAlreadyExistsException("Professor with email " + email + " already exists.");
        }

        professor.setName(name);
        professor.setEmail(email);
        repository.save(professor);
    }

    /** Finds all Professors for a given set of IDs and returns them in a Map for fast lookups */
    @Transactional(readOnly = true)
    public Map<Long, Professor> findByIds(Set<Long> ids) {
        return repository.findAllById(ids).stream()
                .collect(Collectors.toMap(Professor::getId, Function.identity()));
    }
}

class ProfessorNotFoundException extends RuntimeException {
    public ProfessorNotFoundException(String message) {
        super(message);
    }
}

class ProfessorAlreadyExistsException extends RuntimeException {
    public ProfessorAlreadyExistsException(String message) {
        super(message);
    }
}