package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.model.BookCollection;

public record BookCollectionIndex(
        Long id,
        String title,
        String description,
        String userFullName,
        Long userId,
        int bookCount,
        boolean isPublic,
        BookCollection.CollectionType type
) { }
