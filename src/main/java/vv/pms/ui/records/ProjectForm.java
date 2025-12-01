package vv.pms.ui.records;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vv.pms.project.Program;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectForm {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    // Use a List for multi-select binding in thymeleaf
    @NotNull
    private List<Program> programRestrictions = new ArrayList<>();

    @NotNull(message = "Max students required")
    @Min(value = 1, message = "At least 1 student required")
    private Integer requiredStudents;

    public ProjectForm() {}

    public ProjectForm(Long id, String title, String description, List<Program> programRestrictions, Integer requiredStudents) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.programRestrictions = programRestrictions == null ? new ArrayList<>() : programRestrictions;
        this.requiredStudents = requiredStudents;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Program> getProgramRestrictions() { return programRestrictions; }
    public void setProgramRestrictions(List<Program> programRestrictions) {
        this.programRestrictions = programRestrictions == null ? new ArrayList<>() : programRestrictions;
    }

    public Integer getRequiredStudents() { return requiredStudents; }
    public void setRequiredStudents(Integer requiredStudents) { this.requiredStudents = requiredStudents; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectForm)) return false;
        ProjectForm that = (ProjectForm) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
