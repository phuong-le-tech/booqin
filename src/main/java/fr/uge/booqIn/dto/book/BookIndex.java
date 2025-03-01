package fr.uge.booqIn.dto.book;

import java.util.Date;
import java.util.Set;

public record BookIndex(
        Long id,
        String title,
        Set<String> authorNames,
        Date publicationDate
) {
}