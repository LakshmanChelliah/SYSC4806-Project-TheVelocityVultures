package vv.pms.ui.records;

import java.util.List;

/**
 * For the Project Detail page.
 * Contains nested records for ProfessorDTO and StudentDTO as they
 * are specific to this detailed view.
 */
public record ProjectDetailsDTO(
        Long id,
        String title,
        String description,
        String status,
        ProfessorDTO professor,
        List<StudentDTO> assignedStudents,
        int availability,
        Long assignedProfessorId //added
        ) {

    /**
     * A DTO representing a Professor for the UI.
     */
    public record ProfessorDTO(
            Long id,
            String name,
            String email
    ) {}

    /**
     * A DTO representing a Student for the UI.
     */
    public record StudentDTO(
            Long id,
            String name,
            String studentId,
            String email,
            String program
    ) {}
}