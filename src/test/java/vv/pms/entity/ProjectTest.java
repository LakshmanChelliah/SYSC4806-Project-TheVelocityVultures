package vv.pms.entity;

import org.junit.jupiter.api.Test;
import vv.pms.project.Program;
import vv.pms.project.Project;
import vv.pms.project.ProjectStatus;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void testDefaultConstructor() {
        Project project = new Project();
        
        assertNull(project.getId());
        assertNull(project.getTitle());
        assertNull(project.getDescription());
        assertNull(project.getProgramRestrictions());
        assertEquals(0, project.getRequiredStudents());
        assertEquals(ProjectStatus.OPEN, project.getStatus());
    }

    @Test
    void testParameterizedConstructor() {
        Set<Program> programs = Set.of(Program.SOFTWARE_ENGINEERING, Program.COMPUTER_SYSTEMS_ENGINEERING);
        
        Project project = new Project("Test Project", "Description", programs, 3);
        
        assertNull(project.getId()); // ID is null until persisted
        assertEquals("Test Project", project.getTitle());
        assertEquals("Description", project.getDescription());
        assertEquals(programs, project.getProgramRestrictions());
        assertEquals(3, project.getRequiredStudents());
        assertEquals(ProjectStatus.OPEN, project.getStatus());
    }

    @Test
    void testParameterizedConstructorWithId() {
        Set<Program> programs = Set.of(Program.SOFTWARE_ENGINEERING);
        
        Project project = new Project(1L, "Project with ID", "Description", programs, 2);
        
        assertEquals(1L, project.getId());
        assertEquals("Project with ID", project.getTitle());
        assertEquals("Description", project.getDescription());
        assertEquals(programs, project.getProgramRestrictions());
        assertEquals(2, project.getRequiredStudents());
    }

    @Test
    void testSettersAndGetters() {
        Project project = new Project();
        Set<Program> programs = Set.of(Program.ELECTRICAL_ENGINEERING);
        
        project.setId(1L);
        project.setTitle("New Title");
        project.setDescription("New Description");
        project.setProgramRestrictions(programs);
        project.setRequiredStudents(4);
        project.setStatus(ProjectStatus.FULL);
        
        assertEquals(1L, project.getId());
        assertEquals("New Title", project.getTitle());
        assertEquals("New Description", project.getDescription());
        assertEquals(programs, project.getProgramRestrictions());
        assertEquals(4, project.getRequiredStudents());
        assertEquals(ProjectStatus.FULL, project.getStatus());
    }

    @Test
    void testArchive() {
        Project project = new Project("Test", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2);
        assertEquals(ProjectStatus.OPEN, project.getStatus());
        
        project.archive();
        
        assertEquals(ProjectStatus.ARCHIVED, project.getStatus());
    }

    @Test
    void testIsProgramAllowed_allowed() {
        Set<Program> allowedPrograms = Set.of(
            Program.SOFTWARE_ENGINEERING, 
            Program.COMPUTER_SYSTEMS_ENGINEERING
        );
        Project project = new Project("Test", "Desc", allowedPrograms, 2);
        
        assertTrue(project.isProgramAllowed(Program.SOFTWARE_ENGINEERING));
        assertTrue(project.isProgramAllowed(Program.COMPUTER_SYSTEMS_ENGINEERING));
    }

    @Test
    void testIsProgramAllowed_notAllowed() {
        Set<Program> allowedPrograms = Set.of(Program.SOFTWARE_ENGINEERING);
        Project project = new Project("Test", "Desc", allowedPrograms, 2);
        
        assertFalse(project.isProgramAllowed(Program.ELECTRICAL_ENGINEERING));
        assertFalse(project.isProgramAllowed(Program.CIVIL_ENGINEERING));
        assertFalse(project.isProgramAllowed(Program.MECHANICAL_ENGINEERING));
    }

    @Test
    void testAllProgramRestrictions() {
        Set<Program> allPrograms = new HashSet<>();
        allPrograms.add(Program.COMPUTER_SYSTEMS_ENGINEERING);
        allPrograms.add(Program.SOFTWARE_ENGINEERING);
        allPrograms.add(Program.ELECTRICAL_ENGINEERING);
        allPrograms.add(Program.CIVIL_ENGINEERING);
        allPrograms.add(Program.MECHANICAL_ENGINEERING);
        allPrograms.add(Program.BIOMEDICAL_ENGINEERING);
        allPrograms.add(Program.GENERAL_ENGINEERING);
        
        Project project = new Project("Universal Project", "Open to all", allPrograms, 5);
        
        for (Program program : Program.values()) {
            assertTrue(project.isProgramAllowed(program));
        }
    }

    @Test
    void testProjectStatusTransitions() {
        Project project = new Project("Test", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2);
        
        assertEquals(ProjectStatus.OPEN, project.getStatus());
        
        project.setStatus(ProjectStatus.FULL);
        assertEquals(ProjectStatus.FULL, project.getStatus());
        
        project.setStatus(ProjectStatus.ARCHIVED);
        assertEquals(ProjectStatus.ARCHIVED, project.getStatus());
        
        // Can transition back to OPEN
        project.setStatus(ProjectStatus.OPEN);
        assertEquals(ProjectStatus.OPEN, project.getStatus());
    }

    @Test
    void testTitleChange() {
        Project project = new Project("Original Title", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2);
        
        project.setTitle("Updated Title");
        
        assertEquals("Updated Title", project.getTitle());
    }

    @Test
    void testDescriptionChange() {
        Project project = new Project("Title", "Original Description", Set.of(Program.SOFTWARE_ENGINEERING), 2);
        
        project.setDescription("Updated Description with more details");
        
        assertEquals("Updated Description with more details", project.getDescription());
    }

    @Test
    void testRequiredStudentsChange() {
        Project project = new Project("Title", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2);
        
        project.setRequiredStudents(5);
        
        assertEquals(5, project.getRequiredStudents());
    }

    @Test
    void testEmptyProgramRestrictions() {
        Set<Program> emptyPrograms = new HashSet<>();
        Project project = new Project("Test", "Desc", emptyPrograms, 2);
        
        assertTrue(project.getProgramRestrictions().isEmpty());
        assertFalse(project.isProgramAllowed(Program.SOFTWARE_ENGINEERING));
    }

    @Test
    void testModifyProgramRestrictions() {
        Set<Program> initialPrograms = new HashSet<>();
        initialPrograms.add(Program.SOFTWARE_ENGINEERING);
        Project project = new Project("Test", "Desc", initialPrograms, 2);
        
        Set<Program> newPrograms = new HashSet<>();
        newPrograms.add(Program.ELECTRICAL_ENGINEERING);
        newPrograms.add(Program.MECHANICAL_ENGINEERING);
        project.setProgramRestrictions(newPrograms);
        
        assertFalse(project.isProgramAllowed(Program.SOFTWARE_ENGINEERING));
        assertTrue(project.isProgramAllowed(Program.ELECTRICAL_ENGINEERING));
        assertTrue(project.isProgramAllowed(Program.MECHANICAL_ENGINEERING));
    }
}
