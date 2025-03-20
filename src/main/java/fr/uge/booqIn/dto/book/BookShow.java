package fr.uge.booqIn.dto.book;

import fr.uge.booqIn.dto.author.AuthorDto;

import java.util.Date;
import java.util.Set;

public record BookShow(
        Long id,
        Set<String> isbns,
        String title,
        Set<AuthorDto> authors,
        String description,
        Date publicationDate,
        Date createdAt,
        Date updatedAt,
        Set<String> categories
) {
}