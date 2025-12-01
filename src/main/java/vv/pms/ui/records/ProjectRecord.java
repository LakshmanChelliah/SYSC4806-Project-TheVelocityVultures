package vv.pms.ui.records;

import vv.pms.project.Program;
import vv.pms.project.ProjectStatus;

import java.util.Set;

public record ProjectRecord(
        Long id,
        String title,
        String description,
        Set<Program> programRestrictions,
        int requiredStudents,
        ProjectStatus status
) {}
