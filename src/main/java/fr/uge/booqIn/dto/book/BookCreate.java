package fr.uge.booqIn.dto.book;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public record BookCreate(
        @NotBlank(message = "Book title cannot be empty")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,

        @NotEmpty(message = "At least one author is required")
        @Size(max = 10, message = "Maximum of 10 authors allowed")
        List<@NotBlank(message = "Author name cannot be empty") String> authors,

        @Size(max = 5000, message = "Description must be less than 5000 characters")
        String description,

        @Past(message = "Publication date must be in the past")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        Date publicationDate,

        @Size(max = 5, message = "Maximum of 5 ISBN numbers allowed")
        List<String> isbn
) {

    public BookCreate() {
        this("", new ArrayList<>(), "", null, new ArrayList<>());
    }
}