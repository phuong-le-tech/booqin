package fr.uge.booqIn.dto.user;

import fr.uge.booqIn.entity.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Optional;

public record UserUpdate(
        Optional<@Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String> firstName,

        Optional<@Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String> lastName,

        Optional<@Email(message = "Email must be valid") String> email,

        Optional<@Valid Address> address,

        Optional<@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\w\\W]{8,}$",
                message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number") String> password) {

        /**
         * Default constructor creating empty optionals for all fields
         */
        public UserUpdate() {
                this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }
}