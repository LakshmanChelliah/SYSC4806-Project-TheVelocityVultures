package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import vv.pms.project.Program;
import vv.pms.student.Student;
import vv.pms.student.StudentService;
import vv.pms.allocation.AllocationService;
import vv.pms.professor.ProfessorService;

@Controller
public class StudentProfileController {

    private final StudentService studentService;
    private final AllocationService allocationService;
    private final ProfessorService professorService;

    public StudentProfileController(StudentService studentService,
                                    AllocationService allocationService,
                                    ProfessorService professorService) {
        this.studentService = studentService;
        this.allocationService = allocationService;
        this.professorService = professorService;
    }

    // --- Display student profile ---
    @GetMapping("/student/profile")
    public String viewProfile(HttpSession session, Model model) {
        String role = (String) session.getAttribute("currentUserRole");

        if (role == null || !role.equals("STUDENT"))
            return "redirect:/login";

        Long studentId = (Long) session.getAttribute("currentUserId");

        Student student = studentService.findStudentById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        model.addAttribute("student", student);
        model.addAttribute("programs", Program.values());

        // Prepare team information using public service APIs only
        allocationService.findAllocationByStudentId(studentId).ifPresentOrElse(allocation -> {
            Long projectId = allocation.getProjectId();

            // Fetch professor for the project
            allocationService.findAllocationByProjectId(projectId).flatMap(a -> {
                Long profId = a.getProfessorId();
                return professorService.findProfessorById(profId);
            }).ifPresent(p -> {
                // put professor into model via attribute after fetching
                model.addAttribute("teamProfessor", p);
            });

            // Fetch students assigned to the same project
            java.util.List<Long> studentIds = allocationService.findStudentsByProjectId(projectId);
            java.util.Set<Long> idSet = new java.util.HashSet<>(studentIds);
            java.util.Map<Long, Student> studentsMap = studentService.findByIds(idSet);

                // Place the current student first (so 'Me' appears immediately after Professor),
                // then list other students sorted alphabetically by name.
                Student current = studentsMap.get(studentId);
                java.util.List<Student> otherStudents = studentIds.stream()
                    .map(studentsMap::get)
                    .filter(s -> s != null && !s.getId().equals(studentId))
                    .sorted(java.util.Comparator.comparing(Student::getName))
                    .toList();

                java.util.List<Student> teamStudents = new java.util.ArrayList<>();
                if (current != null) teamStudents.add(current);
                teamStudents.addAll(otherStudents);

                model.addAttribute("teamStudents", teamStudents);
            model.addAttribute("currentStudentId", studentId);
            model.addAttribute("hasTeam", true);
            model.addAttribute("projectId", projectId);
        }, () -> {
            model.addAttribute("hasTeam", false);
        });
        return "student_profile";
    }

    // --- Update student profile ---
    @PostMapping("/student/profile")
    public String updateProfile(@Valid @ModelAttribute("student") Student form,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model) {

        String role = (String) session.getAttribute("currentUserRole");

        if (role == null || !role.equals("STUDENT"))
            return "redirect:/login";

        if (bindingResult.hasErrors()) {
            model.addAttribute("programs", Program.values());
            return "student_profile";
        }

        Long studentId = (Long) session.getAttribute("currentUserId");

        try {
            studentService.updateStudent(
                    studentId,
                    form.getName(),
                    form.getStudentId(),
                    form.getEmail(),
                    form.getProgram()
            );
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("programs", Program.values());
            return "student_profile";
        }

        return "redirect:/student/profile";
    }
}
