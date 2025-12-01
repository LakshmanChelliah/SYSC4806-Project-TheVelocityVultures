package vv.pms.ui.records;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginForm {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    public LoginForm() {}

    public LoginForm(String email) { this.email = email; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
