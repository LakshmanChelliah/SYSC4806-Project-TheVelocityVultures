package vv.pms.entity;

import org.junit.jupiter.api.Test;
import vv.pms.professor.Professor;

import static org.junit.jupiter.api.Assertions.*;

public class ProfessorTest {

    @Test
    void testConstructorSetsFields() {
        Professor prof = new Professor("John Doe", "john.doe@example.com");

        assertEquals("John Doe", prof.getName());
        assertEquals("john.doe@example.com", prof.getEmail());
        assertNull(prof.getId()); // id is null until saved in DB
    }

    @Test
    void testSettersAndGetters() {
        Professor prof = new Professor();
        prof.setId(10L);
        prof.setName("Alice");
        prof.setEmail("alice@example.com");

        assertEquals(10L, prof.getId());
        assertEquals("Alice", prof.getName());
        assertEquals("alice@example.com", prof.getEmail());
    }

    @Test
    void testEmailChange() {
        Professor prof = new Professor("Bob", "bob@example.com");
        prof.setEmail("newbob@example.com");

        assertEquals("newbob@example.com", prof.getEmail());
    }

    @Test
    void testNameChange() {
        Professor prof = new Professor("Prof One", "prof@uni.com");
        prof.setName("Prof Two");

        assertEquals("Prof Two", prof.getName());
    }

}
