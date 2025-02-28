package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.dto.user.UserDto;
import fr.uge.booqIn.entity.Book;

import java.util.List;

public record BookCollectionDto(
        Long id,
        String title,
        String description,
        UserDto owner,
        boolean isPublic,
        List<Book> books,
        String createdAt,
        String updatedAt
) { }
