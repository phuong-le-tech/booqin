package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.model.BookCollection;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

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


