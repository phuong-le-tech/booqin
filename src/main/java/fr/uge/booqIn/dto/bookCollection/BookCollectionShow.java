package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.entity.User;

import java.util.Date;
import java.util.Set;

public record BookCollectionShow(
        Long id,
        String name,
        String description,
        Set<BookIndex> books,
        User user,
        Date createdAt,
        Date updatedAt
) {}
