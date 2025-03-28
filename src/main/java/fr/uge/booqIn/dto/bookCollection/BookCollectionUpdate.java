package fr.uge.booqIn.dto.bookCollection;

import jakarta.validation.constraints.Size;

import java.util.Optional;

public record BookCollectionUpdate(
        Optional<@Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters") String> title,

        Optional<@Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters") String> description,

        boolean isPublic
) {}