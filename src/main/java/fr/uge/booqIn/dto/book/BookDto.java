package fr.uge.booqIn.dto.book;

import fr.uge.booqIn.dto.author.AuthorDto;

import java.util.Date;
import java.util.Set;

public record BookDto(
    Long id,
    Set<String> isbns,
    String title,
    Set<AuthorDto> authors,
    String description,
    String publicationDate,
    Date createdAt,
    Date updatedAt
) {}