package vv.pms.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vv.pms.auth.AuthenticationService;
import vv.pms.auth.LoginRecord;
import vv.pms.professor.ProfessorService;
import vv.pms.student.StudentService;
import vv.pms.coordinator.CoordinatorService;
import vv.pms.ui.records.LoginForm;
import vv.pms.ui.records.SignupForm;
import vv.pms.project.Program;

@Controller
@RequestMapping
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ProfessorService professorService;
    private final StudentService studentService;
    private final CoordinatorService coordinatorService;

    public AuthController(AuthenticationService authenticationService,
                          ProfessorService professorService,
                          StudentService studentService,
                          CoordinatorService coordinatorService) {
        this.authenticationService = authenticationService;
        this.professorService = professorService;
        this.studentService = studentService;
        this.coordinatorService = coordinatorService;
    }

    @GetMapping("/signup")
    public String signupPage(Model model, @ModelAttribute("signupForm") SignupForm signupForm) {
        model.addAttribute("signupForm", signupForm == null ? new SignupForm() : signupForm);
        model.addAttribute("programs", Program.values());
        return "signup";
    }

    @PostMapping("/auth/signup")
    public String doSignup(@Valid @ModelAttribute("signupForm") SignupForm form,
                           BindingResult bindingResult,
                           HttpServletRequest request,
                           Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("programs", Program.values());
            return "signup";
        }

        try {
            if ("PROFESSOR".equalsIgnoreCase(form.getRole())) {
                professorService.addProfessor(form.getName(), form.getEmail());

            } else if ("STUDENT".equalsIgnoreCase(form.getRole())) {

                if (form.getStudentId() == null || form.getStudentId().isBlank()
                        || form.getProgram() == null) {

                    model.addAttribute("signupError", "Student ID and Program are required for student accounts.");
                    model.addAttribute("programs", Program.values());
                    return "signup";
                }

                studentService.addStudent(form.getName(), form.getStudentId(), form.getEmail(), form.getProgram());
            } else if ("COORDINATOR".equalsIgnoreCase(form.getRole())) {
                coordinatorService.addCoordinator(form.getName(), form.getEmail());
            } else {
                model.addAttribute("signupError", "Unknown role selected.");
                model.addAttribute("programs", Program.values());
                return "signup";
            }
        } catch (Exception e) {
            model.addAttribute("signupError", e.getMessage());
            model.addAttribute("programs", Program.values());
            return "signup";
        }

        // Auto-login
        authenticationService.authenticateByEmail(form.getEmail()).ifPresent(user -> {
            HttpSession session = request.getSession(true);
            session.setAttribute("currentUserId", user.id());
            session.setAttribute("currentUserName", user.name());
            session.setAttribute("currentUserEmail", user.email());
            session.setAttribute("currentUserRole", user.role());
        });

        return "redirect:/home";
    }

    @GetMapping("/login")
    public String loginPage(Model model, @ModelAttribute("loginForm") LoginForm loginForm) {
        model.addAttribute("loginForm", loginForm == null ? new LoginForm() : loginForm);
        return "login";
    }

    @PostMapping("/auth/login")
    public String doLogin(@Valid @ModelAttribute("loginForm") LoginForm form,
                          BindingResult bindingResult,
                          HttpServletRequest request,
                          Model model) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        var opt = authenticationService.authenticateByEmail(form.getEmail());
        if (opt.isPresent()) {
            LoginRecord user = opt.get();
            HttpSession session = request.getSession(true);
            session.setAttribute("currentUserId", user.id());
            session.setAttribute("currentUserName", user.name());
            session.setAttribute("currentUserEmail", user.email());
            session.setAttribute("currentUserRole", user.role());
            return "redirect:/home";
        }

        model.addAttribute("loginError", "No account found for that email.");
        return "login";
    }

    @GetMapping("/auth/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/login";
    }
}
