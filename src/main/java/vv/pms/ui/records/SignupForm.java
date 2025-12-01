package vv.pms.ui.records;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import vv.pms.project.Program;

public class SignupForm {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String role;

    // Student-only fields
    private String studentId;
    private Program program;

    public SignupForm() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
}