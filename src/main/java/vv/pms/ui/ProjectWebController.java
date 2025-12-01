package vv.pms.ui;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import vv.pms.project.ProjectService;
import vv.pms.project.Project;
import vv.pms.ui.records.ProjectForm;
import vv.pms.ui.records.ProjectRecord;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

import jakarta.servlet.http.HttpSession;
import vv.pms.project.UnauthorizedAccessException;


@RestController
@RequestMapping("/api/projects")
public class ProjectWebController {

    private final ProjectService projectService;

    public ProjectWebController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Helper to get ID/Role
    private Long getCurrentUserId(HttpSession session) {
        Object idObj = session.getAttribute("currentUserId");
        if (idObj instanceof Number) return ((Number) idObj).longValue();
        if (idObj != null) return Long.parseLong(idObj.toString());
        return null;
    }

    private boolean isCoordinator(HttpSession session) {
        Object roleObj = session.getAttribute("currentUserRole");
        return roleObj != null && "COORDINATOR".equalsIgnoreCase(roleObj.toString());
    }

    private ProjectRecord toRecord(Project p) {
        return new ProjectRecord(
                p.getId(), p.getTitle(), p.getDescription(),
                p.getProgramRestrictions(), p.getRequiredStudents(), p.getStatus()
        );
    }

    @GetMapping
    public List<ProjectRecord> findAll() {
        return projectService.findAllProjects().stream()
                .map(this::toRecord)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<ProjectRecord> createProject(@Valid @RequestBody ProjectRecord dto, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        try {
            Project project = projectService.addProject(
                    dto.title(),
                    dto.description(),
                    dto.programRestrictions(),
                    dto.requiredStudents(),
                    currentUserId // <-- Pass Professor ID
            );
            return ResponseEntity.status(201).body(toRecord(project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        try {
            projectService.deleteProject(id, currentUserId, isCoordinator(session));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<ProjectRecord> archiveProject(@PathVariable Long id, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return ResponseEntity.status(401).build();

        try {
            projectService.archiveProject(id, currentUserId, isCoordinator(session));
            Project updated = projectService.findProjectById(id).orElseThrow();
            return ResponseEntity.ok(toRecord(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @GetMapping("/projects")
    public String listProjects(HttpSession session, Model model) {

        model.addAttribute("currentUserName", session.getAttribute("currentUserName"));
        model.addAttribute("currentUserRole", session.getAttribute("currentUserRole"));

        model.addAttribute("projects", projectService.getAllProjects());
        model.addAttribute("currentProject", new Project());
        return "projects";
    }
}