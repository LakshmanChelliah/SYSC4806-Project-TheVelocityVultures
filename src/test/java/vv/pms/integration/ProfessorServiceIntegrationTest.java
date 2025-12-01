package vv.pms.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProfessorServiceIntegrationTest {

    @Autowired
    private ProfessorService professorService;

    @Test
    void addProfessor_success() {
        Professor professor = professorService.addProfessor("Dr. Test", "dr.test@university.edu");
        
        assertNotNull(professor);
        assertNotNull(professor.getId());
        assertEquals("Dr. Test", professor.getName());
        assertEquals("dr.test@university.edu", professor.getEmail());
    }

    @Test
    void addProfessor_duplicateEmail_throws() {
        professorService.addProfessor("Prof One", "duplicate.prof@university.edu");
        
        assertThrows(IllegalArgumentException.class, () ->
                professorService.addProfessor("Prof Two", "duplicate.prof@university.edu")
        );
    }

    @Test
    void findByEmail_success() {
        professorService.addProfessor("Find By Email Prof", "findbyemail@university.edu");
        
        Optional<Professor> found = professorService.findByEmail("findbyemail@university.edu");
        
        assertTrue(found.isPresent());
        assertEquals("Find By Email Prof", found.get().getName());
    }

    @Test
    void findByEmail_notFound() {
        Optional<Professor> found = professorService.findByEmail("nonexistent@university.edu");
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findProfessorById_success() {
        Professor created = professorService.addProfessor("Find By Id Prof", "findbyid@university.edu");
        
        Optional<Professor> found = professorService.findProfessorById(created.getId());
        
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Find By Id Prof", found.get().getName());
    }

    @Test
    void findProfessorById_notFound() {
        Optional<Professor> found = professorService.findProfessorById(99999L);
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findAllProfessors_success() {
        int initialCount = professorService.findAllProfessors().size();
        
        professorService.addProfessor("Prof A", "profa@university.edu");
        professorService.addProfessor("Prof B", "profb@university.edu");
        
        List<Professor> all = professorService.findAllProfessors();
        
        assertEquals(initialCount + 2, all.size());
    }

    @Test
    void deleteProfessor_success() {
        Professor professor = professorService.addProfessor("Delete Prof", "delete@university.edu");
        Long id = professor.getId();
        
        professorService.deleteProfessor(id);
        
        Optional<Professor> found = professorService.findProfessorById(id);
        assertTrue(found.isEmpty());
    }

    @Test
    void deleteProfessor_notFound_throws() {
        assertThrows(RuntimeException.class, () ->
                professorService.deleteProfessor(99999L)
        );
    }

    @Test
    void modifyProfessor_success() {
        Professor professor = professorService.addProfessor("Original Prof", "original@university.edu");
        
        professorService.modifyProfessor(professor.getId(), "Modified Prof", "modified@university.edu");
        
        Optional<Professor> modified = professorService.findProfessorById(professor.getId());
        assertTrue(modified.isPresent());
        assertEquals("Modified Prof", modified.get().getName());
        assertEquals("modified@university.edu", modified.get().getEmail());
    }

    @Test
    void modifyProfessor_nameOnlyChange() {
        Professor professor = professorService.addProfessor("Name Change Prof", "namechange@university.edu");
        
        professorService.modifyProfessor(professor.getId(), "New Name Prof", "namechange@university.edu");
        
        Optional<Professor> modified = professorService.findProfessorById(professor.getId());
        assertTrue(modified.isPresent());
        assertEquals("New Name Prof", modified.get().getName());
        assertEquals("namechange@university.edu", modified.get().getEmail());
    }

    @Test
    void modifyProfessor_emailToExisting_throws() {
        professorService.addProfessor("Prof One", "existing.prof@university.edu");
        Professor prof2 = professorService.addProfessor("Prof Two", "prof2@university.edu");
        
        assertThrows(RuntimeException.class, () ->
                professorService.modifyProfessor(prof2.getId(), "Prof Two", "existing.prof@university.edu")
        );
    }

    @Test
    void modifyProfessor_notFound_throws() {
        assertThrows(RuntimeException.class, () ->
                professorService.modifyProfessor(99999L, "Name", "email@test.com")
        );
    }

    @Test
    void findByIds_success() {
        Professor prof1 = professorService.addProfessor("Prof 1", "prof1@university.edu");
        Professor prof2 = professorService.addProfessor("Prof 2", "prof2@university.edu");
        Professor prof3 = professorService.addProfessor("Prof 3", "prof3@university.edu");
        
        Map<Long, Professor> found = professorService.findByIds(Set.of(prof1.getId(), prof3.getId()));
        
        assertEquals(2, found.size());
        assertTrue(found.containsKey(prof1.getId()));
        assertTrue(found.containsKey(prof3.getId()));
        assertFalse(found.containsKey(prof2.getId()));
    }

    @Test
    void findByIds_emptySet() {
        Map<Long, Professor> found = professorService.findByIds(Set.of());
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findByIds_withNonExistentId() {
        Professor prof = professorService.addProfessor("Existing Prof", "existing@university.edu");
        
        Map<Long, Professor> found = professorService.findByIds(Set.of(prof.getId(), 99999L));
        
        assertEquals(1, found.size());
        assertTrue(found.containsKey(prof.getId()));
    }

    @Test
    void multipleOperations_sequence() {
        // Create
        Professor professor = professorService.addProfessor("Sequence Prof", "sequence@university.edu");
        assertNotNull(professor.getId());
        
        // Read
        Optional<Professor> read = professorService.findProfessorById(professor.getId());
        assertTrue(read.isPresent());
        
        // Update
        professorService.modifyProfessor(professor.getId(), "Updated Sequence Prof", "sequence.updated@university.edu");
        Optional<Professor> updated = professorService.findProfessorById(professor.getId());
        assertEquals("Updated Sequence Prof", updated.get().getName());
        
        // Delete
        professorService.deleteProfessor(professor.getId());
        Optional<Professor> deleted = professorService.findProfessorById(professor.getId());
        assertTrue(deleted.isEmpty());
    }

    @Test
    void addMultipleProfessors_uniqueIds() {
        Professor prof1 = professorService.addProfessor("Unique 1", "unique1@university.edu");
        Professor prof2 = professorService.addProfessor("Unique 2", "unique2@university.edu");
        Professor prof3 = professorService.addProfessor("Unique 3", "unique3@university.edu");
        
        assertNotEquals(prof1.getId(), prof2.getId());
        assertNotEquals(prof2.getId(), prof3.getId());
        assertNotEquals(prof1.getId(), prof3.getId());
    }
}
