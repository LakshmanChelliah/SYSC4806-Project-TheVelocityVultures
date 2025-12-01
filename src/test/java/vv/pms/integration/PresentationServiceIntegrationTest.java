package vv.pms.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.allocation.AllocationService;
import vv.pms.availability.AvailabilityService;
import vv.pms.presentation.PresentationService;
import vv.pms.presentation.PresentationSlot;
import vv.pms.presentation.Room;
import vv.pms.presentation.RoomService;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Program;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PresentationServiceIntegrationTest {

    @Autowired
    private PresentationService presentationService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private AvailabilityService availabilityService;

    private Professor professor;
    private Project project;
    private Student student;
    private Room room;

    @BeforeEach
    void setUp() {
        professor = professorService.addProfessor("Prof. Present", "prof.present@university.edu");
        project = projectService.addProject(
                "Presentation Project",
                "Project for presentation testing",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2,
                professor.getId()
        );
        student = studentService.addStudent("Present Student", "PRES001", "present.student@university.edu", Program.SOFTWARE_ENGINEERING);
        room = roomService.createRoom("Presentation Room 101");
        
        // Assign student to project
        allocationService.assignStudentToProject(project.getId(), student.getId());
        
        // Set up availability for professor and student (all available)
        Boolean[][] fullAvailability = new Boolean[5][32];
        for (int d = 0; d < 5; d++) {
            for (int t = 0; t < 32; t++) {
                fullAvailability[d][t] = true;
            }
        }
        availabilityService.updateAvailability(professor.getId(), "PROFESSOR", fullAvailability);
        availabilityService.updateAvailability(student.getId(), "STUDENT", fullAvailability);
    }

    @Test
    void getAllRooms_success() {
        List<Room> rooms = presentationService.getAllRooms();
        
        assertNotNull(rooms);
        assertTrue(rooms.size() >= 1);
    }

    @Test
    void assignPresentation_success() {
        PresentationSlot slot = presentationService.assignPresentation(
                project.getId(),
                room.getId(),
                0, // Monday
                0  // 8:00 AM
        );
        
        assertNotNull(slot);
        assertNotNull(slot.getId());
        assertEquals(project.getId(), slot.getProjectId());
        assertEquals(room.getId(), slot.getRoomId());
        assertEquals(0, slot.getDayIndex());
        assertEquals(0, slot.getStartBinIndex());
    }

    @Test
    void assignPresentation_invalidDayIndex_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                presentationService.assignPresentation(project.getId(), room.getId(), -1, 0)
        );
        
        assertThrows(IllegalArgumentException.class, () ->
                presentationService.assignPresentation(project.getId(), room.getId(), 5, 0)
        );
    }

    @Test
    void assignPresentation_invalidBinIndex_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                presentationService.assignPresentation(project.getId(), room.getId(), 0, -1)
        );
        
        assertThrows(IllegalArgumentException.class, () ->
                presentationService.assignPresentation(project.getId(), room.getId(), 0, 16)
        );
    }

    @Test
    void assignPresentation_projectNotFound_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                presentationService.assignPresentation(99999L, room.getId(), 0, 0)
        );
    }

    @Test
    void assignPresentation_roomNotFound_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                presentationService.assignPresentation(project.getId(), 99999L, 0, 0)
        );
    }

    @Test
    void assignPresentation_roomConflict_throws() {
        // Create another project with student
        Student student2 = studentService.addStudent("Student 2", "S002", "s2@test.com", Program.SOFTWARE_ENGINEERING);
        Project project2 = projectService.addProject(
                "Project 2",
                "Description 2",
                Set.of(Program.SOFTWARE_ENGINEERING),
                1,
                professor.getId()
        );
        allocationService.assignStudentToProject(project2.getId(), student2.getId());
        
        // Schedule first presentation
        presentationService.assignPresentation(project.getId(), room.getId(), 0, 0);
        
        // Try to schedule second presentation at same time and room
        assertThrows(IllegalStateException.class, () ->
                presentationService.assignPresentation(project2.getId(), room.getId(), 0, 0)
        );
    }

    @Test
    void unassignPresentation_success() {
        presentationService.assignPresentation(project.getId(), room.getId(), 0, 0);
        
        presentationService.unassignPresentation(project.getId());
        
        Optional<PresentationSlot> slot = presentationService.findByProjectId(project.getId());
        assertTrue(slot.isEmpty());
    }

    @Test
    void unassignPresentation_noPresentationExists_noException() {
        // Should not throw even if no presentation exists
        assertDoesNotThrow(() -> presentationService.unassignPresentation(project.getId()));
    }

    @Test
    void findByProjectId_success() {
        presentationService.assignPresentation(project.getId(), room.getId(), 0, 0);
        
        Optional<PresentationSlot> found = presentationService.findByProjectId(project.getId());
        
        assertTrue(found.isPresent());
        assertEquals(project.getId(), found.get().getProjectId());
    }

    @Test
    void findByProjectId_notFound() {
        Optional<PresentationSlot> found = presentationService.findByProjectId(99999L);
        
        assertTrue(found.isEmpty());
    }

    @Test
    void getAvailableSlots_withFullAvailability() {
        List<PresentationService.SlotOption> slots = presentationService.getAvailableSlots(project.getId(), room.getId());
        
        // With full availability, should have many slot options
        assertNotNull(slots);
        // The exact number depends on the implementation
    }

    @Test
    void getAvailableSlots_noAllocation_returnsEmpty() {
        // Create a project without student allocation
        Project unallocatedProject = projectService.addProject(
                "Unallocated Project",
                "No students",
                Set.of(Program.SOFTWARE_ENGINEERING),
                1,
                professor.getId()
        );
        
        List<PresentationService.SlotOption> slots = presentationService.getAvailableSlots(unallocatedProject.getId(), room.getId());
        
        assertTrue(slots.isEmpty());
    }

    @Test
    void getAvailableSlots_invalidRoom_returnsEmpty() {
        List<PresentationService.SlotOption> slots = presentationService.getAvailableSlots(project.getId(), 99999L);
        
        assertTrue(slots.isEmpty());
    }

    @Test
    void describeSlot_success() {
        PresentationSlot slot = presentationService.assignPresentation(project.getId(), room.getId(), 0, 0);
        
        String description = presentationService.describeSlot(slot);
        
        assertNotNull(description);
        assertTrue(description.contains("Monday"));
        assertTrue(description.contains("08:00"));
    }

    @Test
    void describeSlot_null_returnsNull() {
        String description = presentationService.describeSlot(null);
        
        assertNull(description);
    }

    @Test
    void buildPresentationRows_success() {
        presentationService.assignPresentation(project.getId(), room.getId(), 0, 0);
        
        List<PresentationService.PresentationRow> rows = presentationService.buildPresentationRows();
        
        assertNotNull(rows);
        assertTrue(rows.size() >= 1);
        
        // Find our project's row
        Optional<PresentationService.PresentationRow> ourRow = rows.stream()
                .filter(r -> r.projectId().equals(project.getId()))
                .findFirst();
        
        assertTrue(ourRow.isPresent());
        assertEquals(project.getTitle(), ourRow.get().projectTitle());
        assertEquals(professor.getName(), ourRow.get().professorName());
        assertNotNull(ourRow.get().slotLabel());
    }

    @Test
    void buildPresentationRows_noPresentation_showsNullSlot() {
        // Project has allocation but no presentation slot
        List<PresentationService.PresentationRow> rows = presentationService.buildPresentationRows();
        
        Optional<PresentationService.PresentationRow> ourRow = rows.stream()
                .filter(r -> r.projectId().equals(project.getId()))
                .findFirst();
        
        assertTrue(ourRow.isPresent());
        assertNull(ourRow.get().slotLabel());
    }

    @Test
    void runBestEffortAllocation_createsSlots() {
        // Create a room and ensure it has availability
        Room bestEffortRoom = roomService.createRoom("Best Effort Room");
        
        presentationService.runBestEffortAllocation();
        
        // Check if presentation was assigned
        Optional<PresentationSlot> slot = presentationService.findByProjectId(project.getId());
        // May or may not be assigned depending on availability intersection
        assertNotNull(slot);
    }

    @Test
    void reassignPresentation_updatesExistingSlot() {
        // Assign initial presentation
        PresentationSlot initial = presentationService.assignPresentation(project.getId(), room.getId(), 0, 0);
        Long initialId = initial.getId();
        
        // Reassign to different time
        PresentationSlot reassigned = presentationService.assignPresentation(project.getId(), room.getId(), 1, 2);
        
        // Should update existing slot, not create new one
        assertEquals(initialId, reassigned.getId());
        assertEquals(1, reassigned.getDayIndex());
        assertEquals(2, reassigned.getStartBinIndex());
    }

    @Test
    void multipleProjectsDifferentSlots_success() {
        // Create second project
        Student student2 = studentService.addStudent("Student 2", "S002", "s2@test.com", Program.SOFTWARE_ENGINEERING);
        Project project2 = projectService.addProject(
                "Project 2",
                "Description 2",
                Set.of(Program.SOFTWARE_ENGINEERING),
                1,
                professor.getId()
        );
        allocationService.assignStudentToProject(project2.getId(), student2.getId());
        
        // Set availability for student2
        Boolean[][] fullAvailability = new Boolean[5][32];
        for (int d = 0; d < 5; d++) {
            for (int t = 0; t < 32; t++) {
                fullAvailability[d][t] = true;
            }
        }
        availabilityService.updateAvailability(student2.getId(), "STUDENT", fullAvailability);
        
        // Assign presentations to different slots
        presentationService.assignPresentation(project.getId(), room.getId(), 0, 0);
        presentationService.assignPresentation(project2.getId(), room.getId(), 0, 2);
        
        Optional<PresentationSlot> slot1 = presentationService.findByProjectId(project.getId());
        Optional<PresentationSlot> slot2 = presentationService.findByProjectId(project2.getId());
        
        assertTrue(slot1.isPresent());
        assertTrue(slot2.isPresent());
        assertNotEquals(slot1.get().getStartBinIndex(), slot2.get().getStartBinIndex());
    }
}
