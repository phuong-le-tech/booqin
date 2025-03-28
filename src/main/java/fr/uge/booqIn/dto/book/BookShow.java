package fr.uge.booqIn.dto.book;

import fr.uge.booqIn.dto.author.AuthorDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

public record BookShow(
        @Positive(message = "Book ID must be a positive number")
        Long id,

        @NotEmpty(message = "At least one ISBN is required")
        @Size(max = 5, message = "Maximum of 5 ISBNs allowed")
        Set<String> isbns,

        @NotBlank(message = "Book title cannot be empty")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,

        @NotEmpty(message = "At least one author is required")
        @Size(max = 10, message = "Maximum of 10 authors allowed")
        Set<@Valid AuthorDto> authors,

        @Size(max = 5000, message = "Description must be less than 5000 characters")
        String description,

        Date publicationDate,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        @Size(max = 20, message = "Maximum of 20 categories allowed")
        Set<@NotBlank(message = "Category cannot be blank") String> categories,

        @URL(message = "Invalid cover image path URL")
        String coverImagePath,

        @URL(message = "Invalid thumbnail path URL")
        String thumbnailPath
) {}