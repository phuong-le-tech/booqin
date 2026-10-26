package fr.uge.booqIn.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDto(
        @NotBlank(message = "Login cannot be blank")
        @Email(message = "Must be a valid email address")
        @Size(max = 100, message = "Email must be less than 100 characters")
        String login,

        @NotBlank(message = "Password cannot be blank")
        @Size(max = 100, message = "Password must be less than 100 characters")
        String password
) {


}
