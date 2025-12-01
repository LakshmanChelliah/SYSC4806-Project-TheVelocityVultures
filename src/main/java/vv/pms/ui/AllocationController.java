package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vv.pms.allocation.AllocationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/allocations")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyToProject(@RequestParam Long projectId, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();

        if (session == null) {
            resp.put("error", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }

        Object roleObj = session.getAttribute("currentUserRole");
        if (roleObj == null || !"STUDENT".equalsIgnoreCase(roleObj.toString())) {
            resp.put("error", "Only students may apply to projects");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        Object idObj = session.getAttribute("currentUserId");
        if (idObj == null) {
            resp.put("error", "Missing user id in session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }

        Long studentId;
        try {
            if (idObj instanceof Number) studentId = ((Number) idObj).longValue();
            else studentId = Long.parseLong(idObj.toString());
        } catch (Exception e) {
            resp.put("error", "Invalid user id in session");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }

        try {
            allocationService.assignStudentToProject(projectId, studentId);
            resp.put("message", "applied");
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            resp.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    @PostMapping("/unapply")
    public ResponseEntity<Map<String, Object>> unapplyFromProject(@RequestParam Long projectId, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();

        if (session == null) {
            resp.put("error", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }

        Object roleObj = session.getAttribute("currentUserRole");
        if (roleObj == null || !"STUDENT".equalsIgnoreCase(roleObj.toString())) {
            resp.put("error", "Only students may unapply from projects");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        Object idObj = session.getAttribute("currentUserId");
        if (idObj == null) {
            resp.put("error", "Missing user id in session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }

        Long studentId;
        try {
            if (idObj instanceof Number) studentId = ((Number) idObj).longValue();
            else studentId = Long.parseLong(idObj.toString());
        } catch (Exception e) {
            resp.put("error", "Invalid user id in session");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }

        try {
            allocationService.unassignStudentFromProject(projectId, studentId);
            resp.put("message", "unapplied");
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            resp.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }


}
