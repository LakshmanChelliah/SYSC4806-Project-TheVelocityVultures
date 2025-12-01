package vv.pms.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.professor.ProfessorService;
import vv.pms.project.ProjectService;
import vv.pms.student.StudentService;
import vv.pms.student.Student;
import vv.pms.project.Project;

import java.util.List;

@Controller
@RequestMapping("/allocations")
public class AllocationUI {

    private final AllocationService allocationService;
    private final ProfessorService professorService;
    private final ProjectService projectService;
    private final StudentService studentService;

    public AllocationUI(AllocationService allocationService,
                        ProfessorService professorService,
                        ProjectService projectService,
                        StudentService studentService) {
        this.allocationService = allocationService;
        this.professorService = professorService;
        this.projectService = projectService;
        this.studentService = studentService;
    }

    @GetMapping
    public String listAllocations(Model model) {
        List<ProjectAllocation> allocations = allocationService.findAllAllocations();
        List<Project> projects = projectService.findAllProjects();
        var professors = professorService.findAllProfessors();
        List<Student> students = studentService.findAllStudents();

        // Resolve titles/names for UI
        for (ProjectAllocation alloc : allocations) {
            projects.stream()
                    .filter(p -> p.getId().equals(alloc.getProjectId()))
                    .findFirst()
                    .ifPresent(p -> alloc.setProjectTitle(p.getTitle()));

            professors.stream()
                    .filter(prof -> prof.getId().equals(alloc.getProfessorId()))
                    .findFirst()
                    .ifPresent(prof -> alloc.setProfessorName(prof.getName()));
        }

        model.addAttribute("allocations", allocations);
        model.addAttribute("projects", projects);
        model.addAttribute("professors", professors);
        model.addAttribute("students", students);
        return "allocations";
    }

    @PostMapping("/create")
    public String createAllocation(@RequestParam Long projectId,
                                   @RequestParam Long professorId,
                                   Model model) {
        try {
            allocationService.assignProfessorToProject(projectId, professorId);
            return "redirect:/allocations";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return listAllocations(model);
        }
    }

    @PostMapping("/remove")
    public String removeAllocation(@RequestParam Long projectId, Model model) {
        try {
            allocationService.removeProfessorAllocation(projectId);
            return "redirect:/allocations";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return listAllocations(model);
        }
    }

    @PostMapping("/assign-student")
    public String assignStudent(@RequestParam Long projectId,
                                @RequestParam Long studentId,
                                Model model) {
        try {
            allocationService.assignStudentToProject(projectId, studentId);
            return "redirect:/allocations";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return listAllocations(model);
        }
    }

    @PostMapping("/unassign-student")
    public String unassignStudent(@RequestParam Long projectId,
                                  @RequestParam Long studentId,
                                  Model model) {
        try {
            allocationService.unassignStudentFromProject(projectId, studentId);
            return "redirect:/allocations";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return listAllocations(model);
        }
    }

    @PostMapping("/best-effort")
    public String runBestEffort(Model model) {
        allocationService.runBestEffortAllocation();
        return "redirect:/allocations";
    }
}
