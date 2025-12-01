// File: src/main/java/vv/pms/ui/records/ProjectSummaryDTO.java
package vv.pms.ui.records;

/**
 * For the project search/filter list.
 * It is a lightweight, read-only object containing a summary of a project
 */
public record ProjectSummaryDTO(
        Long id,
        String title,
        String status,
        String professorName,
        int spotsAvailable
) {}