package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(HttpSession session) {
        if (session == null) {
            return "redirect:/login";
        }
        Object roleObj = session.getAttribute("currentUserRole");
        if (roleObj == null) {
            return "redirect:/login";
        }
        String role = roleObj.toString();
        if ("PROFESSOR".equalsIgnoreCase(role)) {
            return "redirect:/professors";
        }
        if ("STUDENT".equalsIgnoreCase(role)) {
            return "redirect:/student/profile";
        }
        if ("COORDINATOR".equalsIgnoreCase(role)) {
            return "redirect:/coordinator";
        }
        // default for unrecognized roles
        return "redirect:/projects";
    }
}
