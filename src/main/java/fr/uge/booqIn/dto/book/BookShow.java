package fr.uge.booqIn.dto.book;

import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.entity.BookCollection;

import java.util.Date;
import java.util.List;
import java.util.Set;

public record BookShow(
        Long id,
        Set<String> isbns,
        String title,
        Set<AuthorDto> authors,
        String description,
        Date publicationDate,
        List<BookCollection> collections,
        Date createdAt,
        Date updatedAt
) {
}