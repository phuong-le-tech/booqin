package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.interfaces.BookCollection;

import java.time.LocalDateTime;
import java.util.List;

public record BookCollectionShow(
        Long id,
        String title,
        String description,
        boolean isPublic,
        List<BookIndex> books,
        UserIndex user,
        BookCollection.CollectionType type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}


