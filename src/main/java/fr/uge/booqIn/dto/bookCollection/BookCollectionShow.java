package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.user.UserShow;

import java.util.Date;
import java.util.Set;

public record BookCollectionShow(
        Long id,
        String name,
        String description,
        boolean isPublic,
        Set<BookIndex> books,
        UserShow user,
        Date createdAt,
        Date updatedAt
) {}
