package fr.uge.booqIn.dto.book;

import java.util.Date;
import java.util.Set;

public record BookDetails(
        Long id,
        String title,
        String description,
        Set<String> authorNames,
        Date publicationDate
) { }
