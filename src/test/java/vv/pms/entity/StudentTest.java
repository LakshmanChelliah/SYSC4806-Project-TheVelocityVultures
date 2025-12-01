package vv.pms.entity;

import org.junit.jupiter.api.Test;
import vv.pms.project.Program;
import vv.pms.student.Student;

import static org.junit.jupiter.api.Assertions.*;

public class StudentTest {

    @Test
    void testConstructorSetsFields() {
        Student student = new Student("Lakshman", "101234567", "lakshman@example.com", Program.SOFTWARE_ENGINEERING);

        assertEquals("Lakshman", student.getName());
        assertEquals("101234567", student.getStudentId());
        assertEquals("lakshman@example.com", student.getEmail());
        assertEquals(Program.SOFTWARE_ENGINEERING, student.getProgram());
        assertFalse(student.isHasProject()); // default false
        assertNull(student.getId());
    }

    @Test
    void testSettersWork() {
        Student student = new Student();
        student.setId(1L);
        student.setName("Bob");
        student.setStudentId("200000001");
        student.setEmail("bob@example.com");
        student.setProgram(Program.ELECTRICAL_ENGINEERING);
        student.setHasProject(true);

        assertEquals(1L, student.getId());
        assertEquals("Bob", student.getName());
        assertEquals("200000001", student.getStudentId());
        assertEquals("bob@example.com", student.getEmail());
        assertEquals(Program.ELECTRICAL_ENGINEERING, student.getProgram());
        assertTrue(student.isHasProject());
    }

    @Test
    void testProjectToggle() {
        Student student = new Student("A", "111111111", "a@a.com", Program.SOFTWARE_ENGINEERING);

        assertFalse(student.isHasProject());
        student.setHasProject(true);
        assertTrue(student.isHasProject());
    }
}
