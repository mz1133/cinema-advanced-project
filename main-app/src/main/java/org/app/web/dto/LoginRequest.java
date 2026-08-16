package org.app.web.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username or email can't be empty")
    private String usernameOrEmail;

    @NotBlank(message = "Username or email can't be empty")
    private String password;
}
