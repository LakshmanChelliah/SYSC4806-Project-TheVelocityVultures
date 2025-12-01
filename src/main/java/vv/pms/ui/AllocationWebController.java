package vv.pms.ui;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;

import java.util.List;

@RestController
@RequestMapping("/api/allocations")
public class AllocationWebController {

    private final AllocationService allocationService;

    public AllocationWebController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @GetMapping
    public List<ProjectAllocation> findAll() {
        return allocationService.findAllAllocations();
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectAllocation> findByProject(@PathVariable Long projectId) {
        return allocationService.findAllocationByProjectId(projectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createAllocation(@RequestParam Long projectId,
                                              @RequestParam Long professorId) {
        try {
            ProjectAllocation allocation =
                    allocationService.assignProfessorToProject(projectId, professorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(allocation);
        } catch (RuntimeException e) {
            // includes not found and invalid state exceptions thrown by the service
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> removeAllocation(@PathVariable Long projectId) {
        try {
            allocationService.removeProfessorAllocation(projectId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{projectId}/students/{studentId}")
    public ResponseEntity<?> assignStudent(@PathVariable Long projectId,
                                           @PathVariable Long studentId) {
        try {
            ProjectAllocation allocation =
                    allocationService.assignStudentToProject(projectId, studentId);
            return ResponseEntity.ok(allocation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}/students/{studentId}")
    public ResponseEntity<?> unassignStudent(@PathVariable Long projectId,
                                             @PathVariable Long studentId) {
        try {
            ProjectAllocation allocation =
                    allocationService.unassignStudentFromProject(projectId, studentId);
            return ResponseEntity.ok(allocation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/best-effort")
    public ResponseEntity<?> runBestEffortAllocation() {
        allocationService.runBestEffortAllocation();
        return ResponseEntity.ok("Best-effort allocation completed.");
    }
}
