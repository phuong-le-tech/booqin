package fr.uge.booqIn.dto.bookCollection;

public record BookCollectionIndex(
        Long id,
        String title,
        String description,
        String userFullName,
        Long userId,
        int bookCount,
        boolean isPublic) {
}
