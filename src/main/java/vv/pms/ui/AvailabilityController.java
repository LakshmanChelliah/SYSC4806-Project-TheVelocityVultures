package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vv.pms.availability.Availability;
import vv.pms.availability.AvailabilityService;

@Controller
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public String showAvailability(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("currentUserId");
        String userRole = (String) session.getAttribute("currentUserRole");

        if (userId == null || userRole == null) {
            return "redirect:/login";
        }

        if (!"STUDENT".equals(userRole) && !"PROFESSOR".equals(userRole)) {
            return "redirect:/home";
        }

        Availability availability = availabilityService.getAvailability(userId, userRole);
        model.addAttribute("availability", availability);

        model.addAttribute("days", new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"});
        model.addAttribute("timeSlots", generateTimeLabels());

        return "availability";
    }

    @PostMapping
    public String updateAvailability(HttpSession session, @ModelAttribute Availability availabilityForm) {
        Long userId = (Long) session.getAttribute("currentUserId");
        String userRole = (String) session.getAttribute("currentUserRole");

        if (userId == null || userRole == null) {
            return "redirect:/login";
        }

        if (!"STUDENT".equals(userRole) && !"PROFESSOR".equals(userRole)) {
            return "redirect:/home";
        }

        availabilityService.updateAvailability(userId, userRole, availabilityForm.getTimeslots());

        return "redirect:/availability?success";
    }

    private String[] generateTimeLabels() {
        String[] labels = new String[16];
        int hour = 8;
        int min = 0;

        for (int i = 0; i < 16; i++) {
            int endMin = min + 30;
            int endHour = hour;

            if (endMin >= 60) {
                endMin -= 60;
                endHour++;
            }

            labels[i] = String.format("%02d:%02d-%02d:%02d", hour, min, endHour, endMin);

            min += 30;
            if (min >= 60) {
                min -= 60;
                hour++;
            }
        }

        return labels;
    }
}