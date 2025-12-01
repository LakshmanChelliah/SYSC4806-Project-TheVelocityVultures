package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vv.pms.project.ProjectService;
import vv.pms.project.Project;
import vv.pms.project.Program;
import vv.pms.student.StudentService;
import vv.pms.student.Student;
import vv.pms.allocation.AllocationService;
import vv.pms.report.SystemConfigService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

@Controller
@RequestMapping("/coordinator")
public class CoordinatorController {

    private final StudentService studentService;
    private final ProjectService projectService;
    private final AllocationService allocationService;
    private final SystemConfigService systemConfigService;

    public CoordinatorController(StudentService studentService,
                                 ProjectService projectService,
                                 AllocationService allocationService,
                                 SystemConfigService systemConfigService) {
        this.studentService = studentService;
        this.projectService = projectService;
        this.allocationService = allocationService;
        this.systemConfigService = systemConfigService;
    }

    @GetMapping
    public String coordinatorHome(@RequestParam(name = "status", required = false) String status,
                                   @RequestParam(name = "program", required = false) String program,
                                   @RequestParam(name = "projectId", required = false) Long projectId,
                                   Model model,
                                   HttpSession session) {

        // AuthN basic gate: must be coordinator
        if (session == null || session.getAttribute("currentUserRole") == null
                || !"COORDINATOR".equalsIgnoreCase(session.getAttribute("currentUserRole").toString())) {
            return "redirect:/login";
        }

        // Add deadline info
        model.addAttribute("currentDeadline", systemConfigService.getReportDeadline().orElse(null));
        model.addAttribute("currentUserName", session.getAttribute("currentUserName"));
        model.addAttribute("currentUserRole", session.getAttribute("currentUserRole"));

        List<Student> allStudents = studentService.findAllStudents();
        List<Project> allProjects = projectService.getAllProjects();

        // Build allocation lookup: studentId -> projectTitle
        Map<Long, Long> studentToProjectIds = allocationService.mapStudentToProjectIds();
        Map<Long, String> studentProjectTitles = studentToProjectIds.entrySet().stream()
            .map(e -> Map.entry(e.getKey(), projectService.findProjectById(e.getValue())
                .map(Project::getTitle).orElse("Project " + e.getValue())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Filters
        String normalizedStatus = status == null ? "UNASSIGNED" : status.toUpperCase(); // default show unassigned
          Program programEnum = (program != null && !program.isBlank())
                ? Arrays.stream(Program.values()).filter(p -> p.name().equals(program)).findFirst().orElse(null)
                : null;

        // Use allocation map (studentToProjectIds) as single source of truth for assignment status
        List<Student> filtered = allStudents.stream()
                .filter(s -> {
                    if (programEnum != null && !programEnum.equals(s.getProgram())) return false;
                    boolean assigned = studentToProjectIds.containsKey(s.getId());
                    if ("ASSIGNED".equals(normalizedStatus) && !assigned) return false;
                    if ("UNASSIGNED".equals(normalizedStatus) && assigned) return false;
                    return true;
                })
                .filter(s -> {
                    if (projectId == null || projectId <= 0) return true;
                    Long pid = studentToProjectIds.get(s.getId());
                    return pid != null && pid.equals(projectId);
                })
                .collect(Collectors.toList());

        // Build view model list
        List<Map<String, Object>> studentViews = filtered.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("studentId", s.getStudentId());
            m.put("email", s.getEmail());
            m.put("program", s.getProgram());
            m.put("projectTitle", studentProjectTitles.getOrDefault(s.getId(), "UNASSIGNED"));
            return m;
        }).toList();

        model.addAttribute("students", studentViews);
        model.addAttribute("programs", Program.values());
        model.addAttribute("projects", allProjects);
        model.addAttribute("selectedStatus", normalizedStatus);
        model.addAttribute("selectedProgram", program);
        model.addAttribute("selectedProjectId", projectId);
        return "coordinator";
    }

    @PostMapping("/set-deadline")
    public String setDeadline(@RequestParam("deadline") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline) {
        systemConfigService.setReportDeadline(deadline);
        return "redirect:/coordinator";
    }

}
