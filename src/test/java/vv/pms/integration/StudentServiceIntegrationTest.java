package vv.pms.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.project.Program;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StudentServiceIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Test
    void addStudent_success() {
        Student student = studentService.addStudent(
                "Test Student",
                "TEST001",
                "test.student@university.edu",
                Program.SOFTWARE_ENGINEERING
        );
        
        assertNotNull(student);
        assertNotNull(student.getId());
        assertEquals("Test Student", student.getName());
        assertEquals("TEST001", student.getStudentId());
        assertEquals("test.student@university.edu", student.getEmail());
        assertEquals(Program.SOFTWARE_ENGINEERING, student.getProgram());
        assertFalse(student.isHasProject());
    }

    @Test
    void addStudent_duplicateStudentId_throws() {
        studentService.addStudent("Student 1", "DUP001", "student1@university.edu", Program.SOFTWARE_ENGINEERING);
        
        assertThrows(IllegalArgumentException.class, () ->
                studentService.addStudent("Student 2", "DUP001", "student2@university.edu", Program.SOFTWARE_ENGINEERING)
        );
    }

    @Test
    void findStudentById_success() {
        Student created = studentService.addStudent(
                "Find Test",
                "FIND001",
                "find@university.edu",
                Program.SOFTWARE_ENGINEERING
        );
        
        Optional<Student> found = studentService.findStudentById(created.getId());
        
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Find Test", found.get().getName());
    }

    @Test
    void findStudentById_notFound() {
        Optional<Student> found = studentService.findStudentById(99999L);
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findByEmail_success() {
        studentService.addStudent("Email Test", "EMAIL001", "email.test@university.edu", Program.SOFTWARE_ENGINEERING);
        
        Optional<Student> found = studentService.findByEmail("email.test@university.edu");
        
        assertTrue(found.isPresent());
        assertEquals("Email Test", found.get().getName());
    }

    @Test
    void findByEmail_notFound() {
        Optional<Student> found = studentService.findByEmail("nonexistent@university.edu");
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findAllStudents_success() {
        int initialCount = studentService.findAllStudents().size();
        
        studentService.addStudent("Student A", "STUA001", "studa@university.edu", Program.SOFTWARE_ENGINEERING);
        studentService.addStudent("Student B", "STUB001", "studb@university.edu", Program.ELECTRICAL_ENGINEERING);
        
        List<Student> all = studentService.findAllStudents();
        
        assertEquals(initialCount + 2, all.size());
    }

    @Test
    void updateProjectStatus_setTrue() {
        Student student = studentService.addStudent(
                "Project Status Test",
                "PROJ001",
                "proj@university.edu",
                Program.SOFTWARE_ENGINEERING
        );
        
        assertFalse(student.isHasProject());
        
        studentService.updateProjectStatus(student.getId(), true);
        
        Optional<Student> updated = studentService.findStudentById(student.getId());
        assertTrue(updated.isPresent());
        assertTrue(updated.get().isHasProject());
    }

    @Test
    void updateProjectStatus_setFalse() {
        Student student = studentService.addStudent(
                "Project Status False",
                "PROJF001",
                "projf@university.edu",
                Program.SOFTWARE_ENGINEERING
        );
        
        studentService.updateProjectStatus(student.getId(), true);
        studentService.updateProjectStatus(student.getId(), false);
        
        Optional<Student> updated = studentService.findStudentById(student.getId());
        assertTrue(updated.isPresent());
        assertFalse(updated.get().isHasProject());
    }

    @Test
    void updateProjectStatus_notFound_throws() {
        assertThrows(StudentService.StudentNotFoundException.class, () ->
                studentService.updateProjectStatus(99999L, true)
        );
    }

    @Test
    void findByIds_success() {
        Student s1 = studentService.addStudent("Student 1", "IDS001", "ids1@university.edu", Program.SOFTWARE_ENGINEERING);
        Student s2 = studentService.addStudent("Student 2", "IDS002", "ids2@university.edu", Program.SOFTWARE_ENGINEERING);
        Student s3 = studentService.addStudent("Student 3", "IDS003", "ids3@university.edu", Program.SOFTWARE_ENGINEERING);
        
        Map<Long, Student> found = studentService.findByIds(Set.of(s1.getId(), s3.getId()));
        
        assertEquals(2, found.size());
        assertTrue(found.containsKey(s1.getId()));
        assertTrue(found.containsKey(s3.getId()));
        assertFalse(found.containsKey(s2.getId()));
    }

    @Test
    void findByIds_emptySet() {
        Map<Long, Student> found = studentService.findByIds(Set.of());
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findStudentsWithoutProject_success() {
        Student withProject = studentService.addStudent("With Project", "WP001", "wp@university.edu", Program.SOFTWARE_ENGINEERING);
        Student withoutProject = studentService.addStudent("Without Project", "WOP001", "wop@university.edu", Program.SOFTWARE_ENGINEERING);
        
        studentService.updateProjectStatus(withProject.getId(), true);
        
        List<Student> withoutProjectList = studentService.findStudentsWithoutProject();
        
        assertTrue(withoutProjectList.stream().anyMatch(s -> s.getId().equals(withoutProject.getId())));
        assertFalse(withoutProjectList.stream().anyMatch(s -> s.getId().equals(withProject.getId())));
    }

    @Test
    void updateStudent_success() {
        Student student = studentService.addStudent(
                "Original Name",
                "ORIG001",
                "original@university.edu",
                Program.SOFTWARE_ENGINEERING
        );
        
        Student updated = studentService.updateStudent(
                student.getId(),
                "Updated Name",
                "UPDT001",
                "updated@university.edu",
                Program.ELECTRICAL_ENGINEERING
        );
        
        assertEquals("Updated Name", updated.getName());
        assertEquals("UPDT001", updated.getStudentId());
        assertEquals("updated@university.edu", updated.getEmail());
        assertEquals(Program.ELECTRICAL_ENGINEERING, updated.getProgram());
    }

    @Test
    void updateStudent_nameOnlyChange() {
        Student student = studentService.addStudent(
                "Name Only",
                "NAME001",
                "nameonly@university.edu",
                Program.SOFTWARE_ENGINEERING
        );
        
        Student updated = studentService.updateStudent(
                student.getId(),
                "New Name Only",
                student.getStudentId(),
                student.getEmail(),
                student.getProgram()
        );
        
        assertEquals("New Name Only", updated.getName());
        assertEquals("NAME001", updated.getStudentId());
    }

    @Test
    void updateStudent_duplicateStudentId_throws() {
        studentService.addStudent("Existing", "EXIST001", "existing@university.edu", Program.SOFTWARE_ENGINEERING);
        Student student = studentService.addStudent("Update Target", "UPDT001", "uptarget@university.edu", Program.SOFTWARE_ENGINEERING);
        
        assertThrows(IllegalArgumentException.class, () ->
                studentService.updateStudent(
                        student.getId(),
                        "Update Target",
                        "EXIST001",
                        "uptarget@university.edu",
                        Program.SOFTWARE_ENGINEERING
                )
        );
    }

    @Test
    void updateStudent_duplicateEmail_throws() {
        studentService.addStudent("Existing", "EXEM001", "existing.email@university.edu", Program.SOFTWARE_ENGINEERING);
        Student student = studentService.addStudent("Update Target", "EMUPDT001", "email.target@university.edu", Program.SOFTWARE_ENGINEERING);
        
        assertThrows(IllegalArgumentException.class, () ->
                studentService.updateStudent(
                        student.getId(),
                        "Update Target",
                        "EMUPDT001",
                        "existing.email@university.edu",
                        Program.SOFTWARE_ENGINEERING
                )
        );
    }

    @Test
    void updateStudent_notFound_throws() {
        assertThrows(StudentService.StudentNotFoundException.class, () ->
                studentService.updateStudent(99999L, "Name", "ID", "email@test.com", Program.SOFTWARE_ENGINEERING)
        );
    }

    @Test
    void allProgramTypes() {
        for (Program program : Program.values()) {
            String uniqueId = "PRG" + program.ordinal();
            Student student = studentService.addStudent(
                    "Student " + program.name(),
                    uniqueId,
                    uniqueId + "@university.edu",
                    program
            );
            
            assertEquals(program, student.getProgram());
        }
    }

    @Test
    void multipleOperations_sequence() {
        // Create
        Student student = studentService.addStudent("Sequence", "SEQ001", "seq@university.edu", Program.SOFTWARE_ENGINEERING);
        assertNotNull(student.getId());
        
        // Read
        Optional<Student> read = studentService.findStudentById(student.getId());
        assertTrue(read.isPresent());
        
        // Update
        studentService.updateStudent(student.getId(), "Updated Sequence", "SEQ001", "seq.updated@university.edu", Program.ELECTRICAL_ENGINEERING);
        Optional<Student> updated = studentService.findStudentById(student.getId());
        assertEquals("Updated Sequence", updated.get().getName());
        assertEquals(Program.ELECTRICAL_ENGINEERING, updated.get().getProgram());
    }

    @Test
    void addMultipleStudents_uniqueIds() {
        Student s1 = studentService.addStudent("Unique 1", "UNQ001", "unq1@university.edu", Program.SOFTWARE_ENGINEERING);
        Student s2 = studentService.addStudent("Unique 2", "UNQ002", "unq2@university.edu", Program.SOFTWARE_ENGINEERING);
        Student s3 = studentService.addStudent("Unique 3", "UNQ003", "unq3@university.edu", Program.SOFTWARE_ENGINEERING);
        
        assertNotEquals(s1.getId(), s2.getId());
        assertNotEquals(s2.getId(), s3.getId());
        assertNotEquals(s1.getId(), s3.getId());
    }
}
