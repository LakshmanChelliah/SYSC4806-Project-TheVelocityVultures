package vv.pms.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.allocation.AllocationService;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Program;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.project.ProjectStatus;
import vv.pms.project.UnauthorizedAccessException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProjectServiceIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private AllocationService allocationService;

    private Professor professor;

    @BeforeEach
    void setUp() {
        professor = professorService.addProfessor("Project Prof", "project.prof@university.edu");
    }

    @Test
    void addProject_success() {
        Project project = projectService.addProject(
                "Test Project",
                "Test Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                3,
                professor.getId()
        );
        
        assertNotNull(project);
        assertNotNull(project.getId());
        assertEquals("Test Project", project.getTitle());
        assertEquals("Test Description", project.getDescription());
        assertEquals(3, project.getRequiredStudents());
        assertEquals(ProjectStatus.OPEN, project.getStatus());
        assertTrue(project.getProgramRestrictions().contains(Program.SOFTWARE_ENGINEERING));
    }

    @Test
    void addProject_createsAllocation() {
        Project project = projectService.addProject(
                "Allocation Test",
                "Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2,
                professor.getId()
        );
        
        // Verify allocation was created
        Optional<Long> ownerId = allocationService.findProjectOwnerId(project.getId());
        assertTrue(ownerId.isPresent());
        assertEquals(professor.getId(), ownerId.get());
    }

    @Test
    void addProject_nullTitle_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.addProject(null, "Description", Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId())
        );
    }

    @Test
    void addProject_blankTitle_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.addProject("   ", "Description", Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId())
        );
    }

    @Test
    void addProject_nullDescription_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.addProject("Title", null, Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId())
        );
    }

    @Test
    void addProject_blankDescription_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.addProject("Title", "   ", Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId())
        );
    }

    @Test
    void addProject_nullProfessorId_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.addProject("Title", "Description", Set.of(Program.SOFTWARE_ENGINEERING), 2, null)
        );
    }

    @Test
    void findProjectById_success() {
        Project created = projectService.addProject(
                "Find Test",
                "Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2,
                professor.getId()
        );
        
        Optional<Project> found = projectService.findProjectById(created.getId());
        
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Find Test", found.get().getTitle());
    }

    @Test
    void findProjectById_notFound() {
        Optional<Project> found = projectService.findProjectById(99999L);
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findProjectById_null() {
        Optional<Project> found = projectService.findProjectById(null);
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findAllProjects_success() {
        int initialCount = projectService.findAllProjects().size();
        
        projectService.addProject("Project 1", "Desc 1", Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId());
        projectService.addProject("Project 2", "Desc 2", Set.of(Program.ELECTRICAL_ENGINEERING), 3, professor.getId());
        
        List<Project> all = projectService.findAllProjects();
        
        assertEquals(initialCount + 2, all.size());
    }

    @Test
    void getAllProjects_success() {
        int initialCount = projectService.getAllProjects().size();
        
        projectService.addProject("All Project 1", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId());
        
        List<Project> all = projectService.getAllProjects();
        
        assertEquals(initialCount + 1, all.size());
    }

    @Test
    void findProjectsByIds_success() {
        Project p1 = projectService.addProject("Ids Test 1", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId());
        Project p2 = projectService.addProject("Ids Test 2", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId());
        Project p3 = projectService.addProject("Ids Test 3", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId());
        
        List<Project> found = projectService.findProjectsByIds(Set.of(p1.getId(), p3.getId()));
        
        assertEquals(2, found.size());
    }

    @Test
    void findProjectsByIds_emptySet() {
        List<Project> found = projectService.findProjectsByIds(Set.of());
        
        assertTrue(found.isEmpty());
    }

    @Test
    void findProjectsByIds_null() {
        List<Project> found = projectService.findProjectsByIds(null);
        
        assertTrue(found.isEmpty());
    }

    @Test
    void updateProject_asOwner_success() {
        Project project = projectService.addProject(
                "Update Test",
                "Original Description",
                new HashSet<>(Set.of(Program.SOFTWARE_ENGINEERING)),
                2,
                professor.getId()
        );
        
        project.setDescription("Updated Description");
        Project updated = projectService.updateProject(project, professor.getId(), false);
        
        assertEquals("Updated Description", updated.getDescription());
    }

    @Test
    void updateProject_asCoordinator_success() {
        Project project = projectService.addProject(
                "Coordinator Update",
                "Description",
                new HashSet<>(Set.of(Program.SOFTWARE_ENGINEERING)),
                2,
                professor.getId()
        );
        
        Professor otherProf = professorService.addProfessor("Other Prof", "other@university.edu");
        
        project.setTitle("Updated by Coordinator");
        // Pass different professor ID but with coordinator flag
        Project updated = projectService.updateProject(project, otherProf.getId(), true);
        
        assertEquals("Updated by Coordinator", updated.getTitle());
    }

    @Test
    void updateProject_notOwner_throws() {
        Project project = projectService.addProject(
                "Unauthorized Update",
                "Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2,
                professor.getId()
        );
        
        Professor otherProf = professorService.addProfessor("Other Prof", "other@university.edu");
        
        project.setTitle("Should Fail");
        assertThrows(UnauthorizedAccessException.class, () ->
                projectService.updateProject(project, otherProf.getId(), false)
        );
    }

    @Test
    void updateProject_nullId_throws() {
        Project project = new Project("No Id", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2);
        
        assertThrows(IllegalArgumentException.class, () ->
                projectService.updateProject(project, professor.getId(), false)
        );
    }

    @Test
    void deleteProject_asOwner_success() {
        Project project = projectService.addProject(
                "Delete Test",
                "Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2,
                professor.getId()
        );
        
        projectService.deleteProject(project.getId(), professor.getId(), false);
        
        Optional<Project> found = projectService.findProjectById(project.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    void deleteProject_asCoordinator_success() {
        Project project = projectService.addProject(
                "Coordinator Delete",
                "Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2,
                professor.getId()
        );
        
        Professor otherProf = professorService.addProfessor("Other Prof", "other@university.edu");
        
        projectService.deleteProject(project.getId(), otherProf.getId(), true);
        
        Optional<Project> found = projectService.findProjectById(project.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    void deleteProject_notOwner_throws() {
        Project project = projectService.addProject(
                "Unauthorized Delete",
                "Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2,
                professor.getId()
        );
        
        Professor otherProf = professorService.addProfessor("Other Prof", "other@university.edu");
        
        assertThrows(UnauthorizedAccessException.class, () ->
                projectService.deleteProject(project.getId(), otherProf.getId(), false)
        );
    }

    @Test
    void archiveProject_success() {
        Project project = projectService.addProject(
                "Archive Test",
                "Description",
                new HashSet<>(Set.of(Program.SOFTWARE_ENGINEERING)),
                2,
                professor.getId()
        );
        
        assertEquals(ProjectStatus.OPEN, project.getStatus());
        
        projectService.archiveProject(project.getId(), professor.getId(), false);
        
        Optional<Project> archived = projectService.findProjectById(project.getId());
        assertTrue(archived.isPresent());
        assertEquals(ProjectStatus.ARCHIVED, archived.get().getStatus());
    }

    @Test
    void archiveProject_notFound_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.archiveProject(99999L, professor.getId(), true)
        );
    }

    @Test
    void findProjects_byProgram() {
        projectService.addProject("SE Project", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId());
        projectService.addProject("EE Project", "Desc", Set.of(Program.ELECTRICAL_ENGINEERING), 2, professor.getId());
        
        Page<Project> seProjects = projectService.findProjects("SOFTWARE_ENGINEERING", null, PageRequest.of(0, 10));
        
        assertTrue(seProjects.getContent().stream()
                .allMatch(p -> p.getProgramRestrictions().contains(Program.SOFTWARE_ENGINEERING)));
    }

    @Test
    void findProjects_byStatus() {
        Project openProject = projectService.addProject("Open Project", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId());
        Project archivedProject = projectService.addProject("Archived Project", "Desc", new HashSet<>(Set.of(Program.SOFTWARE_ENGINEERING)), 2, professor.getId());
        projectService.archiveProject(archivedProject.getId(), professor.getId(), false);
        
        Page<Project> openProjects = projectService.findProjects(null, "OPEN", PageRequest.of(0, 10));
        
        assertTrue(openProjects.getContent().stream()
                .allMatch(p -> p.getStatus() == ProjectStatus.OPEN));
    }

    @Test
    void findProjects_pagination() {
        // Create multiple projects
        for (int i = 0; i < 15; i++) {
            projectService.addProject("Pagination Project " + i, "Desc " + i, 
                    Set.of(Program.SOFTWARE_ENGINEERING), 2, professor.getId());
        }
        
        Page<Project> firstPage = projectService.findProjects(null, null, PageRequest.of(0, 5));
        Page<Project> secondPage = projectService.findProjects(null, null, PageRequest.of(1, 5));
        
        assertEquals(5, firstPage.getContent().size());
        assertEquals(5, secondPage.getContent().size());
        assertTrue(firstPage.getTotalElements() >= 15);
    }

    @Test
    void project_multipleProgramRestrictions() {
        Project project = projectService.addProject(
                "Multi Program Project",
                "Description",
                Set.of(Program.SOFTWARE_ENGINEERING, Program.COMPUTER_SYSTEMS_ENGINEERING, Program.ELECTRICAL_ENGINEERING),
                4,
                professor.getId()
        );
        
        Optional<Project> found = projectService.findProjectById(project.getId());
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getProgramRestrictions().size());
    }
}
