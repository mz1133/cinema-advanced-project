package org.app.web.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username cant be empty")
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters!")
    private String username;

    @Email(message = "Must be valid email address")
    @NotBlank(message = "Email is required!")
    private String email;

    @NotBlank(message = "Password cant be empty")
    @Size(min = 7, max = 20, message = "Password must be between 7 and 20 characters!")
    private String password;

    @NotBlank(message = "Password cant be empty")
    @Size(min = 7, max = 20, message = "Password not match!")
    private String confirmPassword;

    @NotBlank(message = "First name cant be empty")
    @Size(min = 3, max = 20, message = "First name must be between 3 and 20 characters!")
    private String firstName;

    @NotBlank(message = "Last name cant be empty")
    @Size(min = 3, max = 20, message = "Last name must be between 3 and 20 characters!")
    private String lastName;
}
