package vv.pms.entity;

import org.junit.jupiter.api.Test;
import vv.pms.allocation.ProjectAllocation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectAllocationTest {

    @Test
    void testDefaultConstructor() {
        ProjectAllocation allocation = new ProjectAllocation();
        
        assertNull(allocation.getId());
        assertNull(allocation.getProjectId());
        assertNull(allocation.getProfessorId());
        assertNotNull(allocation.getAssignedStudentIds());
        assertTrue(allocation.getAssignedStudentIds().isEmpty());
    }

    @Test
    void testParameterizedConstructor() {
        Long projectId = 1L;
        Long professorId = 10L;
        
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        
        assertEquals(projectId, allocation.getProjectId());
        assertEquals(professorId, allocation.getProfessorId());
        assertNotNull(allocation.getAssignedStudentIds());
        assertTrue(allocation.getAssignedStudentIds().isEmpty());
    }

    @Test
    void testAssignStudent() {
        ProjectAllocation allocation = new ProjectAllocation(1L, 10L);
        Long studentId = 100L;
        
        allocation.assignStudent(studentId);
        
        assertTrue(allocation.getAssignedStudentIds().contains(studentId));
        assertEquals(1, allocation.getAssignedStudentIds().size());
    }

    @Test
    void testAssignMultipleStudents() {
        ProjectAllocation allocation = new ProjectAllocation(1L, 10L);
        
        allocation.assignStudent(100L);
        allocation.assignStudent(101L);
        allocation.assignStudent(102L);
        
        assertEquals(3, allocation.getAssignedStudentIds().size());
        assertTrue(allocation.getAssignedStudentIds().contains(100L));
        assertTrue(allocation.getAssignedStudentIds().contains(101L));
        assertTrue(allocation.getAssignedStudentIds().contains(102L));
    }

    @Test
    void testUnassignStudent() {
        ProjectAllocation allocation = new ProjectAllocation(1L, 10L);
        Long studentId = 100L;
        
        allocation.assignStudent(studentId);
        allocation.unassignStudent(studentId);
        
        assertFalse(allocation.getAssignedStudentIds().contains(studentId));
        assertTrue(allocation.getAssignedStudentIds().isEmpty());
    }

    @Test
    void testAddStudent_preventsduplicates() {
        ProjectAllocation allocation = new ProjectAllocation(1L, 10L);
        Long studentId = 100L;
        
        allocation.addStudent(studentId);
        allocation.addStudent(studentId); // try to add again
        
        assertEquals(1, allocation.getAssignedStudentIds().size());
    }

    @Test
    void testRemoveStudent() {
        ProjectAllocation allocation = new ProjectAllocation(1L, 10L);
        Long studentId = 100L;
        
        allocation.addStudent(studentId);
        allocation.removeStudent(studentId);
        
        assertFalse(allocation.getAssignedStudentIds().contains(studentId));
    }

    @Test
    void testSetAssignedStudentIds() {
        ProjectAllocation allocation = new ProjectAllocation(1L, 10L);
        List<Long> studentIds = new ArrayList<>();
        studentIds.add(100L);
        studentIds.add(101L);
        
        allocation.setAssignedStudentIds(studentIds);
        
        assertEquals(2, allocation.getAssignedStudentIds().size());
        assertTrue(allocation.getAssignedStudentIds().contains(100L));
        assertTrue(allocation.getAssignedStudentIds().contains(101L));
    }

    @Test
    void testUIHelperFields() {
        ProjectAllocation allocation = new ProjectAllocation(1L, 10L);
        
        allocation.setProjectTitle("Test Project Title");
        allocation.setProfessorName("Dr. Smith");
        
        assertEquals("Test Project Title", allocation.getProjectTitle());
        assertEquals("Dr. Smith", allocation.getProfessorName());
    }

    @Test
    void testUIHelperFieldsAreTransient() {
        // UI helper fields should be null by default (transient fields are not persisted)
        ProjectAllocation allocation = new ProjectAllocation(1L, 10L);
        
        assertNull(allocation.getProjectTitle());
        assertNull(allocation.getProfessorName());
    }
}
