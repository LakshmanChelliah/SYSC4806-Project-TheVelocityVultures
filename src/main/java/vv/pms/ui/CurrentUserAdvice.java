package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vv.pms.student.StudentService;
import vv.pms.professor.ProfessorService;

@ControllerAdvice
public class CurrentUserAdvice {

    private final StudentService studentService;
    private final ProfessorService professorService;

    public CurrentUserAdvice(StudentService studentService, ProfessorService professorService) {
        this.studentService = studentService;
        this.professorService = professorService;
    }

    @ModelAttribute("currentUserName")
    public String currentUserName(HttpSession session) {
        if (session == null) return null;

        Long id = (Long) session.getAttribute("currentUserId");
        String role = (String) session.getAttribute("currentUserRole");

        if (id == null || role == null) return null;

        if ("STUDENT".equalsIgnoreCase(role)) {
            return studentService.findStudentById(id)
                    .map(s -> s.getName())
                    .orElse(null);
        }

        if ("PROFESSOR".equalsIgnoreCase(role)) {
            return professorService.findProfessorById(id)
                    .map(p -> p.getName())
                    .orElse(null);
        }

        return null;
    }

    @ModelAttribute("currentUserRole")
    public String currentUserRole(HttpSession session) {
        if (session == null) return null;
        Object o = session.getAttribute("currentUserRole");
        return o == null ? null : o.toString();
    }
}

