package fr.uge.booqIn.dto.user;

import fr.uge.booqIn.entity.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Optional;

public record UserUpdate(
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        Optional<String> firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        Optional<String> lastName,

        @Email(message = "Email must be valid")
        Optional<String> email,

        @Valid
        Optional<Address> address,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\w\\W]{8,}$",
                message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number")
        Optional<String> password) {

        /**
         * Default constructor creating empty optionals for all fields
         */
        public UserUpdate() {
                this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        /**
         * Convenience factory method to create from non-optional values
         */
        public static UserUpdate of(String firstName, String lastName, String email, Address address, String password) {
                return new UserUpdate(
                        Optional.ofNullable(firstName),
                        Optional.ofNullable(lastName),
                        Optional.ofNullable(email),
                        Optional.ofNullable(address),
                        Optional.ofNullable(password)
                );
        }
}