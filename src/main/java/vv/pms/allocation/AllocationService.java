package vv.pms.allocation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.allocation.internal.ProjectAllocationRepository;
import vv.pms.professor.ProfessorService;
import vv.pms.project.ProjectService;
import vv.pms.student.StudentService;
import vv.pms.project.Project;
import vv.pms.student.Student;
import vv.pms.project.ProjectOwnershipGateway;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AllocationService implements ProjectOwnershipGateway {

    private final ProjectAllocationRepository repository;
    private final ProfessorService professorService;
    private final ProjectService projectService;
    private final StudentService studentService;

    public AllocationService(
            ProjectAllocationRepository repository,
            ProfessorService professorService,
            ProjectService projectService,
            StudentService studentService) {
        this.repository = repository;
        this.professorService = professorService;
        this.projectService = projectService;
        this.studentService = studentService;
    }

    // --- 1. Method for the Web Controller (Returns ProjectAllocation) ---
    public ProjectAllocation assignProfessorToProject(Long projectId, Long professorId) {
        projectService.findProjectById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project ID " + projectId + " not found."));

        professorService.findProfessorById(professorId)
                .orElseThrow(() -> new ProfessorNotFoundException("Professor ID " + professorId + " not found."));

        if (repository.findByProjectId(projectId).isPresent()) {
            throw new AllocationStateException("Project " + projectId + " is already allocated to a professor.");
        }

        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        return repository.save(allocation);
    }

    // --- 2. Method for the Interface/ProjectService (Returns void) ---
    @Override
    public void assignProjectOwner(Long projectId, Long professorId) {
        assignProfessorToProject(projectId, professorId);
    }

    @Override
    public Optional<Long> findProjectOwnerId(Long projectId) {
        return repository.findByProjectId(projectId)
                .map(ProjectAllocation::getProfessorId);
    }

    // --- Other Methods ---

    public void removeProfessorAllocation(Long projectId) {
        ProjectAllocation allocation = repository.findByProjectId(projectId)
                .orElseThrow(() -> new AllocationNotFoundException("Allocation for Project ID " + projectId + " not found."));
        repository.delete(allocation);
    }

    public ProjectAllocation assignStudentToProject(Long projectId, Long studentId) {
        ProjectAllocation allocation = repository.findByProjectId(projectId)
                .orElseThrow(() -> new AllocationNotFoundException("Project " + projectId + " is not yet allocated."));

        Project project = projectService.findProjectById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project ID " + projectId + " not found."));

        Student student = studentService.findStudentById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student ID " + studentId + " not found."));

        if (allocation.getAssignedStudentIds().contains(studentId)) {
            throw new AllocationStateException("Student " + studentId + " is already assigned to this project.");
        }

        if (student.isHasProject()) {
            throw new AllocationStateException("Student " + studentId + " already has an assigned project.");
        }

        if (allocation.getAssignedStudentIds().size() >= project.getRequiredStudents()) {
            throw new AllocationStateException("Project " + projectId + " is already full.");
        }

        if (!project.isProgramAllowed(student.getProgram())) {
            throw new AllocationStateException("Student's program (" + student.getProgram() + ") does not match restrictions.");
        }

        allocation.addStudent(studentId);
        studentService.updateProjectStatus(studentId, true);

        return repository.save(allocation);
    }

    public ProjectAllocation unassignStudentFromProject(Long projectId, Long studentId) {
        ProjectAllocation allocation = repository.findByProjectId(projectId)
                .orElseThrow(() -> new AllocationNotFoundException("Project " + projectId + " not allocated."));

        if (!allocation.getAssignedStudentIds().contains(studentId)) {
            throw new AllocationNotFoundException("Student " + studentId + " is not assigned to this project.");
        }

        allocation.unassignStudent(studentId);
        studentService.updateProjectStatus(studentId, false);

        return repository.save(allocation);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectAllocation> findAllocationByProjectId(Long projectId) {
        return repository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectAllocation> findAllocationByStudentId(Long studentId) {
        return repository.findAll().stream()
                .filter(a -> a.getAssignedStudentIds().contains(studentId))
                .findFirst();
    }

    @Transactional(readOnly = true)
    public java.util.List<Long> findStudentsByProjectId(Long projectId) {
        return repository.findByProjectId(projectId)
                .map(ProjectAllocation::getAssignedStudentIds)
                .orElse(java.util.List.of());
    }

    /**
     * Returns the list of projects assigned to the given professor.
     */
    @Transactional(readOnly = true)
    public List<Project> findProjectsByProfessorId(Long professorId) {
        List<ProjectAllocation> allocations = repository.findByProfessorId(professorId);
        Set<Long> projectIds = allocations.stream()
                .map(ProjectAllocation::getProjectId)
                .collect(Collectors.toSet());
        
        if (projectIds.isEmpty()) {
            return List.of();
        }
        
        return projectService.findProjectsByIds(projectIds);
    }

    @Transactional
    public void runBestEffortAllocation() {
        List<Project> projects = projectService.findAllProjects();
        List<Student> students = studentService.findAllStudents();

        for (Project project : projects) {
            Optional<ProjectAllocation> existing = repository.findByProjectId(project.getId());
            if (existing.isEmpty()) {
                professorService.findAllProfessors().stream().findFirst().ifPresent(prof -> {
                    try {
                        assignProfessorToProject(project.getId(), prof.getId());
                    } catch (RuntimeException ignored) { }
                });
            }
        }

        students = studentService.findAllStudents();
        List<ProjectAllocation> allocations = repository.findAll();

        for (ProjectAllocation allocation : allocations) {
            Project project = projectService.findProjectById(allocation.getProjectId()).orElse(null);
            if (project == null) continue;

            int capacity = project.getRequiredStudents();
            for (Student student : students) {
                if (student.isHasProject()) continue;
                if (!project.getProgramRestrictions().isEmpty() && !project.getProgramRestrictions().contains(student.getProgram())) continue;
                if (allocation.getAssignedStudentIds().size() >= capacity) break;

                try {
                    assignStudentToProject(project.getId(), student.getId());
                } catch (RuntimeException ignored) { }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ProjectAllocation> findAllAllocations() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<Long, ProjectAllocation> findAllocationsByProjectIds(Set<Long> projectIds) {
        return repository.findByProjectIdIn(projectIds).stream()
                .collect(Collectors.toMap(ProjectAllocation::getProjectId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> mapStudentToProjectIds() {
        return repository.findAll().stream()
                .flatMap(a -> a.getAssignedStudentIds().stream()
                        .map(sid -> new java.util.AbstractMap.SimpleEntry<>(sid, a.getProjectId())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static class AllocationNotFoundException extends RuntimeException {
        public AllocationNotFoundException(String message) { super(message); }
    }
    public static class AllocationStateException extends RuntimeException {
        public AllocationStateException(String message) { super(message); }
    }
    public static class ProjectNotFoundException extends RuntimeException {
        public ProjectNotFoundException(String message) { super(message); }
    }
    public static class ProfessorNotFoundException extends RuntimeException {
        public ProfessorNotFoundException(String message) { super(message); }
    }
    public static class StudentNotFoundException extends RuntimeException {
        public StudentNotFoundException(String message) { super(message); }
    }
}