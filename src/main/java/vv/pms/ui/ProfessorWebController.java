package vv.pms.ui;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import vv.pms.professor.ProfessorService;
import vv.pms.professor.Professor;
import vv.pms.ui.records.ProfessorRecord;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/professors")
public class ProfessorWebController {

    private final ProfessorService professorService;

    // The UI module depends on the ProfessorService from the 'professor' module.
    public ProfessorWebController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    /**
     * GET /api/professors : Lists all professors.
     *
     * @return A list of ProfessorRecord DTOs.
     */
    @GetMapping
    public List<ProfessorRecord> findAll() {
        return professorService.findAllProfessors().stream()
                .map(p -> new ProfessorRecord(p.getId(), p.getName(), p.getEmail()))
                .collect(Collectors.toList());
    }

    /**
     * POST /api/professors : Creates a new professor.
     * @param dto The ProfessorRecord DTO containing the new professor's data.
     * @return The created ProfessorRecord with assigned ID.
     */
    @PostMapping
    public ResponseEntity<ProfessorRecord> createProfessor(@Valid @RequestBody ProfessorRecord dto) {
        try {
            Professor professor = professorService.addProfessor(dto.name(), dto.email());
            ProfessorRecord createdDto = new ProfessorRecord(professor.getId(), professor.getName(), professor.getEmail());
            return ResponseEntity.status(201).body(createdDto);
        } catch (IllegalArgumentException e) {
            // Catches validation errors or duplicates thrown by the Service layer
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/professors/{id} : Updates an existing professor.
     *
     * @param id The ID of the professor to update.
     * @param dto The ProfessorRecord DTO containing updated data.
     * @return The updated ProfessorRecord DTO.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProfessorRecord> updateProfessor(@PathVariable Long id, @Valid @RequestBody ProfessorRecord dto) {
        try {
            professorService.modifyProfessor(id, dto.name(), dto.email());
            Professor updatedProfessor = professorService.findProfessorById(id)
                    .orElseThrow(() -> new RuntimeException("Unexpected error: Professor not found after update."));
            ProfessorRecord updatedDto = new ProfessorRecord(updatedProfessor.getId(), updatedProfessor.getName(), updatedProfessor.getEmail());
            return ResponseEntity.ok(updatedDto);
        } catch (Exception e) {
            // Handles not found or validation errors
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/professors/{id} : Deletes a professor.
     * @param id The ID of the professor to delete.
     * @return A ResponseEntity with appropriate status code.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfessor(@PathVariable Long id) {
        try {
            professorService.deleteProfessor(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Handles if professor is not found or cannot be deleted due to dependencies
            return ResponseEntity.notFound().build();
        }
    }
}
