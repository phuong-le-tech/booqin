package fr.uge.booqIn.dto.cart;

import fr.uge.booqIn.dto.book.BookIndex;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ShopCartDto(
        @NotNull(message = "Cart ID cannot be null")
        @Positive(message = "Cart ID must be a positive number")
        Long id,

        @NotNull(message = "User ID cannot be null")
        @Positive(message = "User ID must be a positive number")
        Long userId,

        @NotNull(message = "Books list cannot be null")
        List<@Valid BookIndex> books
) {
  public ShopCartDto {
    books = books != null ? List.copyOf(books) : List.of();
  }
}