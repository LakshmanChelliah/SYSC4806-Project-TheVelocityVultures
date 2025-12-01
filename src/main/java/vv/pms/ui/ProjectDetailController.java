package vv.pms.ui;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import vv.pms.allocation.ProjectAllocation;
import vv.pms.allocation.AllocationService;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.student.Student;
import vv.pms.student.StudentService;
import vv.pms.ui.records.ProjectDetailsDTO;
import vv.pms.ui.records.ProjectSummaryDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A controller with the singular purpose of serving aggregated, read-only
 * project data (summaries and details) for the UI.
 */
@RestController
@RequestMapping("/api/ui/projects")
public class ProjectDetailController {

    private final ProjectService projectService;
    private final ProfessorService professorService;
    private final AllocationService allocationService;
    private final StudentService studentService;

    public ProjectDetailController(ProjectService projectService,
                                   ProfessorService professorService,
                                   AllocationService allocationService,
                                   StudentService studentService) {
        this.projectService = projectService;
        this.professorService = professorService;
        this.allocationService = allocationService;
        this.studentService = studentService;
    }

    /**
     * Serves the 'summary' view of projects.
     */
    @GetMapping
    public Page<ProjectSummaryDTO> searchProjects(
            @RequestParam(required = false) String program,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        Page<Project> projectsPage = projectService.findProjects(program, status, pageable); //
        List<Project> projects = projectsPage.getContent();

        Set<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toSet());
        if (projectIds.isEmpty()) {
            return Page.empty(pageable); // Return early if no projects
        }

        Map<Long, ProjectAllocation> allocMap = allocationService.findAllocationsByProjectIds(projectIds); //

        // Get all needed Professors at once
        Set<Long> profIds = allocMap.values().stream()
                .map(ProjectAllocation::getProfessorId) //
                .collect(Collectors.toSet());

        Map<Long, Professor> profMap = profIds.isEmpty() ? Collections.emptyMap() :
                professorService.findByIds(profIds); //

        // Map to the Summary DTO
        return projectsPage.map(project -> {

            ProjectAllocation alloc = allocMap.get(project.getId());
            Professor prof = (alloc != null) ? profMap.get(alloc.getProfessorId()) : null;
            String profName = (prof != null) ? prof.getName() : "Unassigned";

            int allocCount = (alloc != null) ? alloc.getAssignedStudentIds().size() : 0; //
            int spotsAvailable = project.getRequiredStudents() - allocCount;

            return new ProjectSummaryDTO(
                    project.getId(),
                    project.getTitle(),
                    project.getStatus().toString(), //
                    profName,
                    spotsAvailable
            );
        });
    }

    /**
     * Serves the 'full detail' view of a project.
     */
    @GetMapping("/details/{id}")
    public ProjectDetailsDTO getProjectDetails(@PathVariable Long id) {

        // Get the main Project
        Project project = projectService.findProjectById(id) //
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id)); // <-- Changed

        // Get the Allocation for this project
        ProjectAllocation allocation = allocationService.findAllocationByProjectId(id) //
                .orElseThrow(() -> new RuntimeException("Allocation not found for project id: " + id)); // <-- Changed

        // Get the Professor
        Professor profEntity = professorService.findProfessorById(allocation.getProfessorId()) //
                .orElseThrow(() -> new RuntimeException("Professor not found with id: " + allocation.getProfessorId())); // <-- Changed

        // Get the Students
        Set<Long> studentIds = (Set<Long>) allocation.getAssignedStudentIds(); //
        List<Student> studentEntities = studentIds.isEmpty() ? Collections.emptyList() :
                List.copyOf(studentService.findByIds(studentIds).values()); //

        // Map Entities to DTOs
        ProjectDetailsDTO.ProfessorDTO professorDTO = new ProjectDetailsDTO.ProfessorDTO(
                profEntity.getId(),
                profEntity.getName(),
                profEntity.getEmail()
        );

        List<ProjectDetailsDTO.StudentDTO> studentDTOs = studentEntities.stream()
                .map(student -> new ProjectDetailsDTO.StudentDTO(
                        student.getId(),
                        student.getName(),
                        student.getStudentId(),
                        student.getEmail(),
                        student.getProgram().toString() //
                ))
                .toList();

        // Build and return the final ProjectDetailsDTO
        int spotsAvailable = project.getRequiredStudents() - studentDTOs.size();

        return new ProjectDetailsDTO(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStatus().toString(), //
                professorDTO,
                studentDTOs,
                spotsAvailable,
                allocation.getProfessorId());
    }
}