// vv.pms.student.StudentService.java

package vv.pms.student;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.student.internal.StudentRepository;

import java.util.List;
import java.util.Optional;

import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import vv.pms.project.Program;

@Service
@Transactional
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates and saves a new student record.
     */
    public Student addStudent(String name, String studentId, String email, Program program) {
        if (repository.findByStudentId(studentId).isPresent()) {
            throw new IllegalArgumentException("Student ID " + studentId + " already exists.");
        }

        Student newStudent = new Student(name, studentId, email, program);
        return repository.save(newStudent);
    }

    /**
     * Retrieves a student by their primary ID. This is the API used by the Allocation module.
     */
    @Transactional(readOnly = true)
    public Optional<Student> findStudentById(Long id) {
        return repository.findById(id);
    }

    /**
     * Find a student by email. Public API used by other modules (e.g., auth).
     */
    @Transactional(readOnly = true)
    public Optional<Student> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    /**
     * Returns all students. Used by allocation UI / best-effort algorithm.
     */
    @Transactional(readOnly = true)
    public List<Student> findAllStudents() {
        return repository.findAll();
    }

    /**
     * Toggles the project status. Used by the Allocation module after a successful assignment.
     */
    public void updateProjectStatus(Long studentId, boolean hasProject) {
        Student student = repository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student ID " + studentId + " not found."));
        student.setHasProject(hasProject);
        repository.save(student);
    }

    /** Finds all Students for a given set of IDs and returns them in a Map for fast lookups. */
    @Transactional(readOnly = true)
    public Map<Long, Student> findByIds(Set<Long> ids) {
        return repository.findAllById(ids).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
    }
    
    @Transactional(readOnly = true)
    public List<Student> findStudentsWithoutProject() {
        return repository.findAll().stream()
                .filter(s -> !s.isHasProject())
                .toList();
    }

    // --- Custom Exception ---
    public static class StudentNotFoundException extends RuntimeException {
        public StudentNotFoundException(String message) {
            super(message);
        }
    }

    public Student updateStudent(Long id, String name, String studentId, String email, vv.pms.project.Program program) {
        Student s = repository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student ID " + id + " not found."));
        // If changing studentId/email check uniqueness
        if (!s.getStudentId().equals(studentId) && repository.findByStudentId(studentId).isPresent()) {
            throw new IllegalArgumentException("Student ID " + studentId + " already exists.");
        }
        if (!s.getEmail().equals(email) && repository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email " + email + " already exists.");
        }
        s.setName(name);
        s.setStudentId(studentId);
        s.setEmail(email);
        s.setProgram(program);
        return repository.save(s);
    }
}
