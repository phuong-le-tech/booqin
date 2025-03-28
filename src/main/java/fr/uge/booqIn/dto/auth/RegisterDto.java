package fr.uge.booqIn.dto.auth;

import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.enums.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record RegisterDto(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        @Pattern(regexp = "^[A-Za-zÀ-ÿ'-]+$", message = "First name can only contain letters, hyphens, and apostrophes")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        @Pattern(regexp = "^[A-Za-zÀ-ÿ'-]+$", message = "Last name can only contain letters, hyphens, and apostrophes")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must be less than 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "Password must contain at least one letter, one number, and one special character"
        )
        String password,

        @Valid
        @NotNull(message = "Address is required")
        Address address,

        @NotNull(message = "User role is required")
        UserRole role
) {
}
