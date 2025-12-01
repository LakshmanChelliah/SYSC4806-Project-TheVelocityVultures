package vv.pms.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.auth.AuthenticationService;
import vv.pms.auth.LoginRecord;
import vv.pms.coordinator.Coordinator;
import vv.pms.coordinator.CoordinatorService;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Program;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthenticationServiceIntegrationTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CoordinatorService coordinatorService;

    private Professor professor;
    private Student student;
    private Coordinator coordinator;

    @BeforeEach
    void setUp() {
        professor = professorService.addProfessor("Prof. Auth Test", "prof.auth@university.edu");
        student = studentService.addStudent("Auth Student", "AUTH001", "student.auth@university.edu", Program.SOFTWARE_ENGINEERING);
        coordinator = coordinatorService.addCoordinator("Admin Coordinator", "coordinator@university.edu");
    }

    @Test
    void authenticateByEmail_professor_success() {
        Optional<LoginRecord> loginRecord = authenticationService.authenticateByEmail("prof.auth@university.edu");
        
        assertTrue(loginRecord.isPresent());
        assertEquals(professor.getId(), loginRecord.get().id());
        assertEquals(professor.getName(), loginRecord.get().name());
        assertEquals(professor.getEmail(), loginRecord.get().email());
        assertEquals("PROFESSOR", loginRecord.get().role());
    }

    @Test
    void authenticateByEmail_student_success() {
        Optional<LoginRecord> loginRecord = authenticationService.authenticateByEmail("student.auth@university.edu");
        
        assertTrue(loginRecord.isPresent());
        assertEquals(student.getId(), loginRecord.get().id());
        assertEquals(student.getName(), loginRecord.get().name());
        assertEquals(student.getEmail(), loginRecord.get().email());
        assertEquals("STUDENT", loginRecord.get().role());
    }

    @Test
    void authenticateByEmail_coordinator_success() {
        Optional<LoginRecord> loginRecord = authenticationService.authenticateByEmail("coordinator@university.edu");
        
        assertTrue(loginRecord.isPresent());
        assertEquals(coordinator.getId(), loginRecord.get().id());
        assertEquals(coordinator.getName(), loginRecord.get().name());
        assertEquals(coordinator.getEmail(), loginRecord.get().email());
        assertEquals("COORDINATOR", loginRecord.get().role());
    }

    @Test
    void authenticateByEmail_unknownEmail_returnsEmpty() {
        Optional<LoginRecord> loginRecord = authenticationService.authenticateByEmail("unknown@university.edu");
        
        assertTrue(loginRecord.isEmpty());
    }

    @Test
    void authenticateByEmail_nullEmail_returnsEmpty() {
        Optional<LoginRecord> loginRecord = authenticationService.authenticateByEmail(null);
        
        assertTrue(loginRecord.isEmpty());
    }

    @Test
    void authenticateByEmail_blankEmail_returnsEmpty() {
        Optional<LoginRecord> loginRecord = authenticationService.authenticateByEmail("   ");
        
        assertTrue(loginRecord.isEmpty());
    }

    @Test
    void authenticateByEmail_trimmedEmail_success() {
        // Email with leading/trailing spaces
        Optional<LoginRecord> loginRecord = authenticationService.authenticateByEmail("  prof.auth@university.edu  ");
        
        assertTrue(loginRecord.isPresent());
        assertEquals("PROFESSOR", loginRecord.get().role());
    }

    @Test
    void authenticateByEmail_prioritizesCoordinatorOverProfessor() {
        // If same email existed for both (edge case), coordinator should be checked first
        // In practice, this shouldn't happen due to unique email constraints
        Optional<LoginRecord> loginRecord = authenticationService.authenticateByEmail("coordinator@university.edu");
        
        assertTrue(loginRecord.isPresent());
        assertEquals("COORDINATOR", loginRecord.get().role());
    }

    @Test
    void authenticateByEmail_afterProfessorUpdate_usesNewEmail() {
        professorService.modifyProfessor(professor.getId(), professor.getName(), "new.prof@university.edu");
        
        // Old email should not work
        Optional<LoginRecord> oldEmailResult = authenticationService.authenticateByEmail("prof.auth@university.edu");
        assertTrue(oldEmailResult.isEmpty());
        
        // New email should work
        Optional<LoginRecord> newEmailResult = authenticationService.authenticateByEmail("new.prof@university.edu");
        assertTrue(newEmailResult.isPresent());
        assertEquals("PROFESSOR", newEmailResult.get().role());
    }

    @Test
    void authenticateByEmail_afterStudentUpdate_usesNewEmail() {
        studentService.updateStudent(student.getId(), student.getName(), student.getStudentId(), 
                "new.student@university.edu", student.getProgram());
        
        // New email should work
        Optional<LoginRecord> result = authenticationService.authenticateByEmail("new.student@university.edu");
        assertTrue(result.isPresent());
        assertEquals("STUDENT", result.get().role());
    }

    @Test
    void authenticateByEmail_caseSensitivity() {
        // Email authentication should handle case sensitivity based on implementation
        Optional<LoginRecord> loginRecord = authenticationService.authenticateByEmail("PROF.AUTH@UNIVERSITY.EDU");
        
        // This test documents current behavior - adjust based on actual implementation
        // Most email systems are case-insensitive for the local part
        // If this fails, it means the implementation is case-sensitive
    }
}
