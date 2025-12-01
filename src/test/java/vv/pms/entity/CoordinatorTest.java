package vv.pms.entity;

import org.junit.jupiter.api.Test;
import vv.pms.coordinator.Coordinator;

import static org.junit.jupiter.api.Assertions.*;

class CoordinatorTest {

    @Test
    void testDefaultConstructor() {
        Coordinator coordinator = new Coordinator();
        
        assertNull(coordinator.getId());
        assertNull(coordinator.getName());
        assertNull(coordinator.getEmail());
    }

    @Test
    void testParameterizedConstructor() {
        String name = "John Smith";
        String email = "john.smith@university.edu";
        
        Coordinator coordinator = new Coordinator(name, email);
        
        assertNull(coordinator.getId()); // ID is null until persisted
        assertEquals(name, coordinator.getName());
        assertEquals(email, coordinator.getEmail());
    }

    @Test
    void testSettersAndGetters() {
        Coordinator coordinator = new Coordinator();
        
        coordinator.setId(1L);
        coordinator.setName("Jane Doe");
        coordinator.setEmail("jane.doe@university.edu");
        
        assertEquals(1L, coordinator.getId());
        assertEquals("Jane Doe", coordinator.getName());
        assertEquals("jane.doe@university.edu", coordinator.getEmail());
    }

    @Test
    void testNameChange() {
        Coordinator coordinator = new Coordinator("Original Name", "coordinator@test.com");
        
        coordinator.setName("New Name");
        
        assertEquals("New Name", coordinator.getName());
    }

    @Test
    void testEmailChange() {
        Coordinator coordinator = new Coordinator("Coordinator", "old@test.com");
        
        coordinator.setEmail("new@test.com");
        
        assertEquals("new@test.com", coordinator.getEmail());
    }

    @Test
    void testIdAssignment() {
        Coordinator coordinator = new Coordinator("Test", "test@test.com");
        assertNull(coordinator.getId());
        
        coordinator.setId(100L);
        
        assertEquals(100L, coordinator.getId());
    }

    @Test
    void testMultipleCoordinatorsWithDifferentIds() {
        Coordinator coordinator1 = new Coordinator("Coordinator 1", "coord1@test.com");
        Coordinator coordinator2 = new Coordinator("Coordinator 2", "coord2@test.com");
        
        coordinator1.setId(1L);
        coordinator2.setId(2L);
        
        assertNotEquals(coordinator1.getId(), coordinator2.getId());
        assertNotEquals(coordinator1.getName(), coordinator2.getName());
        assertNotEquals(coordinator1.getEmail(), coordinator2.getEmail());
    }
}
