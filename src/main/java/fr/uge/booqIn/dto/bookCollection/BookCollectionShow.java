package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.user.UserIndex;

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
        String collectionType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}


