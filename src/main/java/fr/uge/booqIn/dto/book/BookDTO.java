package fr.uge.booqIn.dto.book;
import java.util.List;

public record BookDTO(
    Long id,
    String title,
    List<String> authors,
    String isbn,
    String description,
    String publicationDate,
    String createdAt,
    String updatedAt
) { }
