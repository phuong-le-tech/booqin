package fr.uge.booqIn.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartDto(
        @NotNull(message = "User ID cannot be null")
        @Positive(message = "User ID must be a positive number")
        Long userId,

        @NotNull(message = "Book ID cannot be null")
        @Positive(message = "Book ID must be a positive number")
        Long bookId
) {}