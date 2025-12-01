package vv.pms.project;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Entity
@Table(name = "project_topics")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob // Used for storing large amounts of text
    @NotBlank(message = "Description is required")
    @Column(nullable = false)
    private String description;

    @ElementCollection(targetClass = Program.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "topic_program_restrictions", joinColumns = @JoinColumn(name = "topic_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "program")
    private Set<Program> programRestrictions;

    @Column(nullable = false)
    private int requiredStudents; // Maximum number of students allowed

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.OPEN; // OPEN, FULL, ARCHIVED

    public Project() {}

    public Project(String title, String description, Set<Program> programRestrictions, int requiredStudents) {
        this.title = title;
        this.description = description;
        this.programRestrictions = programRestrictions;
        this.requiredStudents = requiredStudents;
    }

    public Project(Long id, String title, String description, Set<Program> programs, int requiredStudents) {
        this.id = id;
        this.title = title;
        this.description =description;
        this.programRestrictions = programs;
        this.requiredStudents = requiredStudents;
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = (Long) id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Program> getProgramRestrictions() {
        return programRestrictions;
    }

    public void setProgramRestrictions(Set<Program> programRestrictions) {
        this.programRestrictions = programRestrictions;
    }

    public int getRequiredStudents() {
        return requiredStudents;
    }

    public void setRequiredStudents(int requiredStudents) {
        this.requiredStudents = requiredStudents;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
    }

    public boolean isProgramAllowed(Program studentProgram) {
        return this.programRestrictions.contains(studentProgram);
    }

    public void setTitle(String title) {
        this.title = title;
    }
}