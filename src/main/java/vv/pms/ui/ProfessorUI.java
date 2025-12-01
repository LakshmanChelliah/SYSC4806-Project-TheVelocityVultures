package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vv.pms.allocation.AllocationService;
import vv.pms.project.Project;

import java.util.List;

/**
 * Serves the application for Professor dashboard.
 */
@Controller
public class ProfessorUI {

    private final AllocationService allocationService;

    public ProfessorUI(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    /**
     * GET /professors : Lists projects assigned to the current professor.
     */
    @GetMapping("/professors")
    public String viewAssignedProjects(Model model, HttpSession session) {
        // Check if user is a professor
        Object roleObj = session.getAttribute("currentUserRole");
        if (roleObj == null || !"PROFESSOR".equalsIgnoreCase(roleObj.toString())) {
            return "redirect:/login";
        }

        Long professorId;
        try {
            Object idObj = session.getAttribute("currentUserId");
            if (idObj instanceof Number) {
                professorId = ((Number) idObj).longValue();
            } else {
                professorId = Long.parseLong(idObj.toString());
            }
        } catch (Exception e) {
            return "redirect:/login";
        }

        List<Project> assignedProjects = allocationService.findProjectsByProfessorId(professorId);
        model.addAttribute("projects", assignedProjects);

        return "professors";
    }
}
