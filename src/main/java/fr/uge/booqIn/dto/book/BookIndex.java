package fr.uge.booqIn.dto.book;



import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

public record BookIndex(
        @Positive(message = "Book ID must be a positive number")
        Long id,

        @NotBlank(message = "Book title cannot be empty")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,

        @NotEmpty(message = "At least one author name is required")
        @Size(max = 10, message = "Maximum of 10 author names allowed")
        Set<@NotBlank(message = "Author name cannot be blank") String> authorNames,

        Date publicationDate,

        @URL(message = "Invalid thumbnail path URL")
        String thumbnailPath
) {}
