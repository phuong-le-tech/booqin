package fr.uge.booqIn.dto.book;

public record BookDTO(
        Long id,
        String title,
        String author,
        String isbn,
        String description,
        String publicationDate,
        String createdAt,
        String updatedAt
) { }
