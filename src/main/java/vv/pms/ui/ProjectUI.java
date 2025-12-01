package vv.pms.ui;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

import vv.pms.allocation.ProjectAllocation;
import vv.pms.allocation.AllocationService;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Program;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.project.ProjectStatus;
import vv.pms.student.Student;
import vv.pms.student.StudentService;
import vv.pms.ui.records.ProjectDetailsDTO;
import vv.pms.ui.records.ProjectForm;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vv.pms.report.ReportService;
import vv.pms.report.ReportSubmission;
import vv.pms.report.SystemConfigService;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

//import vv.pms.project.UnauthorizedAccessException;

@Controller
@RequestMapping("/projects")
public class ProjectUI {

    private final ProjectService projectService;
    private final ProfessorService professorService;
    private final AllocationService allocationService;
    private final StudentService studentService;
    private final ReportService reportService;
    private final SystemConfigService systemConfigService;

    public ProjectUI(ProjectService projectService,
                     ProfessorService professorService,
                     AllocationService allocationService,
                     StudentService studentService,
                     ReportService reportService,
                     SystemConfigService systemConfigService) {
        this.projectService = projectService;
        this.professorService = professorService;
        this.allocationService = allocationService;
        this.studentService = studentService;
        this.reportService = reportService;
        this.systemConfigService = systemConfigService;
    }

    private record ProjectSummary(
            Long id,
            String title,
            String status,
            String professorName,
            int spotsAvailable
    ) {}

    @GetMapping
    public String listProjects(HttpSession session,
                               Model model,
                               @RequestParam(required = false) String program,
                               @RequestParam(required = false) String status,
                               Pageable pageable) {

        // Header Fix
        model.addAttribute("currentUserName", session.getAttribute("currentUserName"));
        model.addAttribute("currentUserRole", session.getAttribute("currentUserRole"));

        Page<Project> projectsPage = projectService.findProjects(program, status, pageable);

        List<Project> projects = projectsPage.getContent();
        Set<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toSet());

        Map<Long, ProjectAllocation> allocMap = projectIds.isEmpty() ? Collections.emptyMap() :
                allocationService.findAllocationsByProjectIds(projectIds);

        Set<Long> profIds = allocMap.values().stream()
                .map(ProjectAllocation::getProfessorId)
                .collect(Collectors.toSet());

        Map<Long, Professor> profMap = profIds.isEmpty() ? Collections.emptyMap() :
                professorService.findByIds(profIds);

        Page<ProjectSummary> summaryPage = projectsPage.map(project -> {
            ProjectAllocation alloc = allocMap.get(project.getId());
            Professor prof = (alloc != null) ? profMap.get(alloc.getProfessorId()) : null;
            String profName = (prof != null) ? prof.getName() : "Unassigned";
            int allocCount = (alloc != null) ? alloc.getAssignedStudentIds().size() : 0;
            int spotsAvailable = project.getRequiredStudents() - allocCount;

            return new ProjectSummary(
                    project.getId(),
                    project.getTitle(),
                    project.getStatus().toString(),
                    profName,
                    spotsAvailable
            );
        });

        model.addAttribute("projectsPage", summaryPage);
        model.addAttribute("programs", Program.values());
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("selectedProgram", program);
        model.addAttribute("selectedStatus", status);

        model.addAttribute("projectForm", new ProjectForm());

        return "projects";
    }


    @GetMapping("/details/{id}")
    public String projectDetails(@PathVariable Long id, Model model, HttpSession session) {

        model.addAttribute("currentUserName", session.getAttribute("currentUserName"));
        model.addAttribute("currentUserRole", session.getAttribute("currentUserRole"));

        // Get the Project
        Project project = projectService.findProjectById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + id));

        // Find the Allocation
        Optional<ProjectAllocation> allocationOpt = allocationService.findAllocationByProjectId(id);

        ProjectDetailsDTO.ProfessorDTO professorDTO;
        List<ProjectDetailsDTO.StudentDTO> studentDTOs;
        int spotsAvailable;
        Long assignedProfessorId = null; // <--- 1. Initialize variable

        if (allocationOpt.isPresent()) {
            // Allocation EXISTS
            ProjectAllocation allocation = allocationOpt.get();
            assignedProfessorId = allocation.getProfessorId(); // <--- 2. Capture the ID

            // Get Professor
            Professor profEntity = professorService.findProfessorById(allocation.getProfessorId())
                    .orElseThrow(() -> new IllegalArgumentException("Professor not found, but allocation exists!"));
            professorDTO = new ProjectDetailsDTO.ProfessorDTO(
                    profEntity.getId(), profEntity.getName(), profEntity.getEmail());

            // Get Students
            List<Long> studentIds = allocation.getAssignedStudentIds();
            List<Student> studentEntities = (studentIds == null || studentIds.isEmpty()) ? Collections.emptyList() :
                    List.copyOf(studentService.findByIds(Set.copyOf(studentIds)).values());
            studentDTOs = studentEntities.stream()
                    .map(student -> new ProjectDetailsDTO.StudentDTO(
                            student.getId(), student.getName(), student.getStudentId(),
                            student.getEmail(), student.getProgram().toString()
                    )).toList();

            spotsAvailable = project.getRequiredStudents() - studentDTOs.size();

        } else {
            // Allocation is MISSING
            professorDTO = new ProjectDetailsDTO.ProfessorDTO(null, "Unassigned", "");
            studentDTOs = Collections.emptyList();
            spotsAvailable = project.getRequiredStudents();
            assignedProfessorId = null; // Ensure it's null if no allocation
        }

        // Create the final DTO
        ProjectDetailsDTO detailsDTO = new ProjectDetailsDTO(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStatus().toString(),
                professorDTO,
                studentDTOs,
                spotsAvailable,
                assignedProfessorId // <--- 3. Pass it to the constructor
        );

        // Create the ProjectForm for the Edit Modal
        ProjectForm form = new ProjectForm(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                new ArrayList<>(project.getProgramRestrictions()),
                project.getRequiredStudents()
        );

        // Determine current user's assignment state
        boolean currentUserIsStudent = false;
        Long currentUserId = null;
        if (session != null && session.getAttribute("currentUserRole") != null) {
            Object roleObj = session.getAttribute("currentUserRole");
            if (roleObj != null && "STUDENT".equalsIgnoreCase(roleObj.toString())) {
                currentUserIsStudent = true;
            }
            Object idObj = session.getAttribute("currentUserId");
            if (idObj != null) {
                try {
                    if (idObj instanceof Number) currentUserId = ((Number) idObj).longValue();
                    else currentUserId = Long.parseLong(idObj.toString());
                } catch (Exception ignored) {
                }
            }
        }

        boolean currentUserAssigned = false;
        if (currentUserId != null) {
            for (ProjectDetailsDTO.StudentDTO s : studentDTOs) {
                if (currentUserId.equals(s.id())) { currentUserAssigned = true; break; }
            }
        }

        boolean currentUserIsAssignedProfessor = false;
        if (currentUserId != null && professorDTO.id() != null && currentUserId.equals(professorDTO.id())) {
             Object r = session.getAttribute("currentUserRole");
             if (r != null && "PROFESSOR".equalsIgnoreCase(r.toString())) {
                 currentUserIsAssignedProfessor = true;
             }
        }

        // Add all data to the Model
        model.addAttribute("project", detailsDTO);
        model.addAttribute("projectForm", form);
        model.addAttribute("programs", Program.values());
        model.addAttribute("currentUserIsStudent", currentUserIsStudent);
        model.addAttribute("currentUserAssigned", currentUserAssigned);
        model.addAttribute("currentUserIsAssignedProfessor", currentUserIsAssignedProfessor);
        model.addAttribute("spotsAvailable", spotsAvailable);
        model.addAttribute("reportDeadline", systemConfigService.getReportDeadline().orElse(null));

        // Report Info
        Optional<ReportSubmission> reportOpt = reportService.getReportByProject(id);
        model.addAttribute("reportSubmission", reportOpt.orElse(null));

        String submissionStatus = "Not assigned to a project";
        boolean canSubmit = false;
        if (currentUserIsStudent && currentUserId != null) {
             submissionStatus = reportService.getSubmissionStatus(currentUserId);
             canSubmit = reportService.canStudentSubmit(currentUserId);
             // Also check if student is assigned to THIS project
             if (canSubmit) {
                 boolean assignedToThis = false;
                 for (ProjectDetailsDTO.StudentDTO s : studentDTOs) {
                    if (currentUserId.equals(s.id())) { assignedToThis = true; break; }
                 }
                 if (!assignedToThis) {
                     canSubmit = false;
                     submissionStatus = "You must be assigned to this project to submit your report";
                 }
             }
        }
        model.addAttribute("submissionStatus", submissionStatus);
        model.addAttribute("canSubmit", canSubmit);

        return "project-details";
    }

    @PostMapping("/apply")
    public String applyToProject(@RequestParam Long projectId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session == null) {
            redirectAttributes.addFlashAttribute("applyError", "You must be logged in to apply.");
            return "redirect:/projects/details/" + projectId;
        }
        Object roleObj = session.getAttribute("currentUserRole");
        if (roleObj == null || !"STUDENT".equalsIgnoreCase(roleObj.toString())) {
            redirectAttributes.addFlashAttribute("applyError", "Only students may apply to projects.");
            return "redirect:/projects/details/" + projectId;
        }
        Object idObj = session.getAttribute("currentUserId");
        if (idObj == null) {
            redirectAttributes.addFlashAttribute("applyError", "Missing user id in session.");
            return "redirect:/projects/details/" + projectId;
        }
        Long studentId;
        try {
            if (idObj instanceof Number) studentId = ((Number) idObj).longValue();
            else studentId = Long.parseLong(idObj.toString());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("applyError", "Invalid user id in session.");
            return "redirect:/projects/details/" + projectId;
        }

        try {
            allocationService.assignStudentToProject(projectId, studentId);
            return "redirect:/projects/details/" + projectId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("applyError", e.getMessage());
            return "redirect:/projects/details/" + projectId;
        }
    }

    @PostMapping("/unapply")
    public String unapplyFromProject(@RequestParam Long projectId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session == null) {
            redirectAttributes.addFlashAttribute("applyError", "You must be logged in to unapply.");
            return "redirect:/projects/details/" + projectId;
        }
        Object roleObj = session.getAttribute("currentUserRole");
        if (roleObj == null || !"STUDENT".equalsIgnoreCase(roleObj.toString())) {
            redirectAttributes.addFlashAttribute("applyError", "Only students may unapply from projects.");
            return "redirect:/projects/details/" + projectId;
        }
        Object idObj = session.getAttribute("currentUserId");
        if (idObj == null) {
            redirectAttributes.addFlashAttribute("applyError", "Missing user id in session.");
            return "redirect:/projects/details/" + projectId;
        }
        Long studentId;
        try {
            if (idObj instanceof Number) studentId = ((Number) idObj).longValue();
            else studentId = Long.parseLong(idObj.toString());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("applyError", "Invalid user id in session.");
            return "redirect:/projects/details/" + projectId;
        }

        try {
            allocationService.unassignStudentFromProject(projectId, studentId);
            return "redirect:/projects/details/" + projectId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("applyError", e.getMessage());
            return "redirect:/projects/details/" + projectId;
        }
    }

    // --- HELPER METHODS FOR SESSION ---
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

     @PostMapping("/upload")
    public String uploadReport(@RequestParam("projectId") Long projectId,
                               @RequestParam("file") MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Object idObj = session.getAttribute("currentUserId");
        if (idObj == null) {
             redirectAttributes.addFlashAttribute("uploadError", "You must be logged in.");
             return "redirect:/projects/details/" + projectId;
        }
        Long studentId;
        try {
            if (idObj instanceof Number) studentId = ((Number) idObj).longValue();
            else studentId = Long.parseLong(idObj.toString());
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("uploadError", "Invalid user session.");
             return "redirect:/projects/details/" + projectId;
        }

        try {
            reportService.submitReport(projectId, studentId, file.getOriginalFilename(), file);
            redirectAttributes.addFlashAttribute("uploadSuccess", "Report submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("uploadError", e.getMessage());
        }
        return "redirect:/projects/details/" + projectId;
    }

    @GetMapping("/download/{projectId}")
    public Object downloadReport(@PathVariable Long projectId, HttpSession session, RedirectAttributes redirectAttributes) {
        // Security Check: Ensure user is authorized to download
        Object idObj = session.getAttribute("currentUserId");
        Object roleObj = session.getAttribute("currentUserRole");
        
        if (idObj == null || roleObj == null) {
            redirectAttributes.addFlashAttribute("downloadError", "You must be logged in to download reports.");
            return "redirect:/projects/details/" + projectId;
        }

        Long currentUserId;
        try {
             if (idObj instanceof Number) currentUserId = ((Number) idObj).longValue();
             else currentUserId = Long.parseLong(idObj.toString());
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("downloadError", "Invalid session.");
             return "redirect:/projects/details/" + projectId;
        }
        String role = roleObj.toString();

        boolean isAuthorized = false;
        Optional<ProjectAllocation> allocationOpt = allocationService.findAllocationByProjectId(projectId);
        
        if (allocationOpt.isPresent()) {
            ProjectAllocation allocation = allocationOpt.get();
            // Allow if assigned professor
            if ("PROFESSOR".equalsIgnoreCase(role) && allocation.getProfessorId().equals(currentUserId)) {
                isAuthorized = true;
            }
            // Allow if assigned student
            if ("STUDENT".equalsIgnoreCase(role) && allocation.getAssignedStudentIds().contains(currentUserId)) {
                isAuthorized = true;
            }
        }

        if (!isAuthorized) {
            redirectAttributes.addFlashAttribute("downloadError", "You are not authorized to download this report.");
            return "redirect:/projects/details/" + projectId;
        }

        Optional<ReportSubmission> reportOpt = reportService.getReportByProject(projectId);
        if (reportOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("downloadError", "No report found for this project.");
            return "redirect:/projects/details/" + projectId;
        }
        ReportSubmission report = reportOpt.get();
        try {
            Path path = Paths.get(report.getFilePath());
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getFilename() + "\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            } else {
                redirectAttributes.addFlashAttribute("downloadError", "Failed to download file");
                return "redirect:/projects/details/" + projectId;
            }
        } catch (MalformedURLException e) {
            redirectAttributes.addFlashAttribute("downloadError", "Error: " + e.getMessage());
            return "redirect:/projects/details/" + projectId;
        }
    }

    @PostMapping
    public String handleProjectForm(@Valid @ModelAttribute("projectForm") ProjectForm form,
                                    BindingResult result, Model model, HttpSession session) {
        // Security Check
        Long currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return "redirect:/login"; // Or handle error

        if (result.hasErrors()) {
            model.addAttribute("projectsPage", projectService.findProjects(null, null, Pageable.unpaged()));
            model.addAttribute("programs", Program.values());
            model.addAttribute("statuses", ProjectStatus.values());
            return "projects";
        }

        if (form.getId() == null) {
            // CREATE: Pass currentUserId as the owner
            Project p = projectService.addProject(
                    form.getTitle(),
                    form.getDescription(),
                    new HashSet<>(form.getProgramRestrictions()),
                    form.getRequiredStudents(),
                    currentUserId // <-- ADDED: Assign Owner
            );
            return "redirect:/projects/details/" + p.getId();

        } else {
            // UPDATE: Check Authorization via Service
            Project p = projectService.findProjectById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Project not found: " + form.getId()));

            p.setTitle(form.getTitle());
            p.setDescription(form.getDescription());
            p.setRequiredStudents(form.getRequiredStudents());
            p.setProgramRestrictions(new HashSet<>(form.getProgramRestrictions()));

            // Pass requesting ID and Coordinator status
            projectService.updateProject(p, currentUserId, isCoordinator(session));

            return "redirect:/projects/details/" + p.getId();
        }
    }

    @PostMapping("/delete")
    public String deleteProject(@RequestParam Long id, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return "redirect:/login";

        projectService.deleteProject(id, currentUserId, isCoordinator(session));
        return "redirect:/projects";
    }

    @PostMapping("/archive")
    public String archiveProject(@RequestParam Long id, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return "redirect:/login";

        projectService.archiveProject(id, currentUserId, isCoordinator(session));
        return "redirect:/projects/details/" + id;
    }
}