package vv.pms.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Program;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AllocationServiceIntegrationTest {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private StudentService studentService;

    private Professor professor;
    private Project project;
    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        professor = professorService.addProfessor("Prof. Test", "prof.test@university.edu");
        project = projectService.addProject(
                "Test Project",
                "Test Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2,
                professor.getId()
        );
        student1 = studentService.addStudent("Student One", "S001", "student1@test.com", Program.SOFTWARE_ENGINEERING);
        student2 = studentService.addStudent("Student Two", "S002", "student2@test.com", Program.SOFTWARE_ENGINEERING);
    }

    @Test
    void assignProfessorToProject_automaticOnProjectCreation() {
        // Project should already be allocated to professor on creation
        Optional<ProjectAllocation> allocation = allocationService.findAllocationByProjectId(project.getId());
        
        assertTrue(allocation.isPresent());
        assertEquals(professor.getId(), allocation.get().getProfessorId());
    }

    @Test
    void assignProfessorToProject_duplicateThrows() {
        // Project is already allocated (from addProject)
        assertThrows(AllocationService.AllocationStateException.class, () ->
                allocationService.assignProfessorToProject(project.getId(), professor.getId())
        );
    }

    @Test
    void assignStudentToProject_success() {
        ProjectAllocation allocation = allocationService.assignStudentToProject(project.getId(), student1.getId());
        
        assertTrue(allocation.getAssignedStudentIds().contains(student1.getId()));
        
        // Verify student's hasProject flag is updated
        Student updatedStudent = studentService.findStudentById(student1.getId()).orElseThrow();
        assertTrue(updatedStudent.isHasProject());
    }

    @Test
    void assignStudentToProject_multipleStudents() {
        allocationService.assignStudentToProject(project.getId(), student1.getId());
        ProjectAllocation allocation = allocationService.assignStudentToProject(project.getId(), student2.getId());
        
        assertEquals(2, allocation.getAssignedStudentIds().size());
        assertTrue(allocation.getAssignedStudentIds().contains(student1.getId()));
        assertTrue(allocation.getAssignedStudentIds().contains(student2.getId()));
    }

    @Test
    void assignStudentToProject_projectFull_throws() {
        allocationService.assignStudentToProject(project.getId(), student1.getId());
        allocationService.assignStudentToProject(project.getId(), student2.getId());
        
        Student student3 = studentService.addStudent("Student Three", "S003", "student3@test.com", Program.SOFTWARE_ENGINEERING);
        
        assertThrows(AllocationService.AllocationStateException.class, () ->
                allocationService.assignStudentToProject(project.getId(), student3.getId())
        );
    }

    @Test
    void assignStudentToProject_duplicateStudent_throws() {
        allocationService.assignStudentToProject(project.getId(), student1.getId());
        
        assertThrows(AllocationService.AllocationStateException.class, () ->
                allocationService.assignStudentToProject(project.getId(), student1.getId())
        );
    }

    @Test
    void assignStudentToProject_studentAlreadyHasProject_throws() {
        allocationService.assignStudentToProject(project.getId(), student1.getId());
        
        // Create another project
        Project project2 = projectService.addProject(
                "Another Project",
                "Another Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2,
                professor.getId()
        );
        
        assertThrows(AllocationService.AllocationStateException.class, () ->
                allocationService.assignStudentToProject(project2.getId(), student1.getId())
        );
    }

    @Test
    void assignStudentToProject_programMismatch_throws() {
        Student eeStudent = studentService.addStudent("EE Student", "S004", "ee@test.com", Program.ELECTRICAL_ENGINEERING);
        
        assertThrows(AllocationService.AllocationStateException.class, () ->
                allocationService.assignStudentToProject(project.getId(), eeStudent.getId())
        );
    }

    @Test
    void unassignStudentFromProject_success() {
        allocationService.assignStudentToProject(project.getId(), student1.getId());
        
        ProjectAllocation allocation = allocationService.unassignStudentFromProject(project.getId(), student1.getId());
        
        assertFalse(allocation.getAssignedStudentIds().contains(student1.getId()));
        
        // Verify student's hasProject flag is updated
        Student updatedStudent = studentService.findStudentById(student1.getId()).orElseThrow();
        assertFalse(updatedStudent.isHasProject());
    }

    @Test
    void unassignStudentFromProject_notAssigned_throws() {
        assertThrows(AllocationService.AllocationNotFoundException.class, () ->
                allocationService.unassignStudentFromProject(project.getId(), student1.getId())
        );
    }

    @Test
    void removeProfessorAllocation_success() {
        allocationService.removeProfessorAllocation(project.getId());
        
        Optional<ProjectAllocation> allocation = allocationService.findAllocationByProjectId(project.getId());
        assertTrue(allocation.isEmpty());
    }

    @Test
    void findAllocationByStudentId_success() {
        allocationService.assignStudentToProject(project.getId(), student1.getId());
        
        Optional<ProjectAllocation> allocation = allocationService.findAllocationByStudentId(student1.getId());
        
        assertTrue(allocation.isPresent());
        assertEquals(project.getId(), allocation.get().getProjectId());
    }

    @Test
    void findStudentsByProjectId_success() {
        allocationService.assignStudentToProject(project.getId(), student1.getId());
        allocationService.assignStudentToProject(project.getId(), student2.getId());
        
        List<Long> studentIds = allocationService.findStudentsByProjectId(project.getId());
        
        assertEquals(2, studentIds.size());
        assertTrue(studentIds.contains(student1.getId()));
        assertTrue(studentIds.contains(student2.getId()));
    }

    @Test
    void findProjectsByProfessorId_success() {
        List<Project> projects = allocationService.findProjectsByProfessorId(professor.getId());
        
        assertEquals(1, projects.size());
        assertEquals(project.getId(), projects.get(0).getId());
    }

    @Test
    void findAllAllocations_success() {
        List<ProjectAllocation> allocations = allocationService.findAllAllocations();
        
        assertFalse(allocations.isEmpty());
    }

    @Test
    void findAllocationsByProjectIds_success() {
        Map<Long, ProjectAllocation> allocations = allocationService.findAllocationsByProjectIds(Set.of(project.getId()));
        
        assertEquals(1, allocations.size());
        assertTrue(allocations.containsKey(project.getId()));
    }

    @Test
    void mapStudentToProjectIds_success() {
        allocationService.assignStudentToProject(project.getId(), student1.getId());
        
        Map<Long, Long> studentToProject = allocationService.mapStudentToProjectIds();
        
        assertEquals(project.getId(), studentToProject.get(student1.getId()));
    }

    @Test
    void runBestEffortAllocation_allocatesStudents() {
        // Create unallocated students
        Student student3 = studentService.addStudent("Student Three", "S003", "s3@test.com", Program.SOFTWARE_ENGINEERING);
        Student student4 = studentService.addStudent("Student Four", "S004", "s4@test.com", Program.SOFTWARE_ENGINEERING);
        
        allocationService.runBestEffortAllocation();
        
        // Check if students are allocated
        List<Long> allocatedStudents = allocationService.findStudentsByProjectId(project.getId());
        assertTrue(allocatedStudents.size() > 0);
    }

    @Test
    void findProjectOwnerId_success() {
        Optional<Long> ownerId = allocationService.findProjectOwnerId(project.getId());
        
        assertTrue(ownerId.isPresent());
        assertEquals(professor.getId(), ownerId.get());
    }
}
