package fr.uge.booqIn.dto.book;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

public record BookIndex(
        Long id,
        String title,
        Set<String> authorNames,
        Date publicationDate,
        String thumbnailPath
) {
}