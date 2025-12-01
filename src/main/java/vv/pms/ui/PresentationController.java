package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vv.pms.presentation.PresentationService;
import vv.pms.presentation.Room;
import vv.pms.presentation.RoomService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/presentations")
public class PresentationController {

    private final PresentationService presentationService;
    private final RoomService roomService;

    public PresentationController(PresentationService presentationService,
                                  RoomService roomService) {
        this.presentationService = presentationService;
        this.roomService = roomService;
    }

    @GetMapping
    public String showPresentations(HttpSession session, Model model,
                                    @RequestParam(value = "error", required = false) String error) {

        Object roleObj = session.getAttribute("currentUserRole");
        if (roleObj == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUserName", session.getAttribute("currentUserName"));
        model.addAttribute("currentUserRole", session.getAttribute("currentUserRole"));


        List<Room> rooms = roomService.getAllRooms();
        var rows = presentationService.buildPresentationRows();

        Map<Long, List<PresentationService.SlotOption>> slotOptionsByProject = new HashMap<>();
        for (var row : rows) {
            Long roomId = row.roomId();
            if (roomId != null) {
                slotOptionsByProject.put(
                        row.projectId(),
                        presentationService.getAvailableSlots(row.projectId(), roomId)
                );
            } else if (!rooms.isEmpty()) {
                Long defaultRoomId = rooms.get(0).getId();
                slotOptionsByProject.put(
                        row.projectId(),
                        presentationService.getAvailableSlots(row.projectId(), defaultRoomId)
                );
            }
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("rows", rows);
        model.addAttribute("slotOptionsByProject", slotOptionsByProject);
        model.addAttribute("error", error);

        return "presentations";
    }

    // ---------- Rooms ----------

    @PostMapping("/rooms/add")
    public String addRoom(@RequestParam("name") String name) {
        try {
            roomService.createRoom(name);
            return "redirect:/presentations";
        } catch (Exception e) {
            return "redirect:/presentations?error=" + e.getMessage().replace(" ", "%20");
        }
    }

    @PostMapping("/rooms/update")
    public String updateRoom(@RequestParam("id") Long id,
                             @RequestParam("name") String name) {
        try {
            roomService.updateRoom(id, name);
            return "redirect:/presentations";
        } catch (Exception e) {
            return "redirect:/presentations?error=" + e.getMessage().replace(" ", "%20");
        }
    }

    @PostMapping("/rooms/delete")
    public String deleteRoom(@RequestParam("id") Long id) {
        roomService.deleteRoom(id);
        return "redirect:/presentations";
    }

    // ---------- Assign / Unassign ----------

    @PostMapping("/assign")
    public String assign(@RequestParam("projectId") Long projectId,
                         @RequestParam("roomId") Long roomId,
                         @RequestParam("slotKey") String slotKey) {
        try {
            String[] parts = slotKey.split("-");
            int dayIndex = Integer.parseInt(parts[0]);
            int startBinIndex = Integer.parseInt(parts[1]);
            presentationService.assignPresentation(projectId, roomId, dayIndex, startBinIndex);
            return "redirect:/presentations";
        } catch (Exception e) {
            return "redirect:/presentations?error=" + e.getMessage().replace(" ", "%20");
        }
    }

    @PostMapping("/unassign")
    public String unassign(@RequestParam("projectId") Long projectId) {
        presentationService.unassignPresentation(projectId);
        return "redirect:/presentations";
    }

    // ---------- Best-effort auto assignment ----------

    @PostMapping("/auto")
    public String autoAssign() {
        presentationService.runBestEffortAllocation();
        return "redirect:/presentations";
    }
}