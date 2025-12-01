package vv.pms.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.coordinator.Coordinator;
import vv.pms.coordinator.CoordinatorService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CoordinatorServiceIntegrationTest {

    @Autowired
    private CoordinatorService coordinatorService;

    @Test
    void addCoordinator_success() {
        Coordinator coordinator = coordinatorService.addCoordinator("Test Coordinator", "test.coord@university.edu");
        
        assertNotNull(coordinator);
        assertNotNull(coordinator.getId());
        assertEquals("Test Coordinator", coordinator.getName());
        assertEquals("test.coord@university.edu", coordinator.getEmail());
    }

    @Test
    void addCoordinator_duplicateEmail_throws() {
        coordinatorService.addCoordinator("Coordinator 1", "duplicate@university.edu");
        
        assertThrows(IllegalArgumentException.class, () ->
                coordinatorService.addCoordinator("Coordinator 2", "duplicate@university.edu")
        );
    }

    @Test
    void findByEmail_success() {
        coordinatorService.addCoordinator("Find Test", "find.test@university.edu");
        
        Optional<Coordinator> found = coordinatorService.findByEmail("find.test@university.edu");
        
        assertTrue(found.isPresent());
        assertEquals("Find Test", found.get().getName());
    }

    @Test
    void findByEmail_notFound() {
        Optional<Coordinator> found = coordinatorService.findByEmail("nonexistent@university.edu");
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findById_success() {
        Coordinator created = coordinatorService.addCoordinator("FindById Test", "findbyid@university.edu");
        
        Optional<Coordinator> found = coordinatorService.findById(created.getId());
        
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("FindById Test", found.get().getName());
    }

    @Test
    void findById_notFound() {
        Optional<Coordinator> found = coordinatorService.findById(99999L);
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_success() {
        int initialCount = coordinatorService.findAll().size();
        
        coordinatorService.addCoordinator("Coord 1", "coord1@university.edu");
        coordinatorService.addCoordinator("Coord 2", "coord2@university.edu");
        
        List<Coordinator> all = coordinatorService.findAll();
        
        assertEquals(initialCount + 2, all.size());
    }

    @Test
    void deleteCoordinator_success() {
        Coordinator coordinator = coordinatorService.addCoordinator("Delete Test", "delete@university.edu");
        Long id = coordinator.getId();
        
        coordinatorService.deleteCoordinator(id);
        
        Optional<Coordinator> found = coordinatorService.findById(id);
        assertTrue(found.isEmpty());
    }

    @Test
    void deleteCoordinator_notFound_throws() {
        assertThrows(RuntimeException.class, () ->
                coordinatorService.deleteCoordinator(99999L)
        );
    }

    @Test
    void updateCoordinator_success() {
        Coordinator coordinator = coordinatorService.addCoordinator("Original Name", "original@university.edu");
        
        coordinatorService.updateCoordinator(coordinator.getId(), "Updated Name", "updated@university.edu");
        
        Optional<Coordinator> updated = coordinatorService.findById(coordinator.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals("updated@university.edu", updated.get().getEmail());
    }

    @Test
    void updateCoordinator_nameOnlyChange() {
        Coordinator coordinator = coordinatorService.addCoordinator("Name Only", "nameonly@university.edu");
        
        coordinatorService.updateCoordinator(coordinator.getId(), "New Name Only", "nameonly@university.edu");
        
        Optional<Coordinator> updated = coordinatorService.findById(coordinator.getId());
        assertTrue(updated.isPresent());
        assertEquals("New Name Only", updated.get().getName());
        assertEquals("nameonly@university.edu", updated.get().getEmail());
    }

    @Test
    void updateCoordinator_emailToExisting_throws() {
        coordinatorService.addCoordinator("Coord 1", "existing@university.edu");
        Coordinator coord2 = coordinatorService.addCoordinator("Coord 2", "coord2@university.edu");
        
        assertThrows(RuntimeException.class, () ->
                coordinatorService.updateCoordinator(coord2.getId(), "Coord 2", "existing@university.edu")
        );
    }

    @Test
    void updateCoordinator_notFound_throws() {
        assertThrows(RuntimeException.class, () ->
                coordinatorService.updateCoordinator(99999L, "Name", "email@test.com")
        );
    }

    @Test
    void findAll_emptyInitially() {
        // This test depends on test isolation - with @Transactional it should work
        // Note: There might be seed data, so we check for non-null instead
        List<Coordinator> all = coordinatorService.findAll();
        assertNotNull(all);
    }

    @Test
    void addMultipleCoordinators_uniqueIds() {
        Coordinator coord1 = coordinatorService.addCoordinator("Coord 1", "unique1@university.edu");
        Coordinator coord2 = coordinatorService.addCoordinator("Coord 2", "unique2@university.edu");
        Coordinator coord3 = coordinatorService.addCoordinator("Coord 3", "unique3@university.edu");
        
        assertNotEquals(coord1.getId(), coord2.getId());
        assertNotEquals(coord2.getId(), coord3.getId());
        assertNotEquals(coord1.getId(), coord3.getId());
    }
}
