package fr.uge.booqIn.dto.book;

import fr.uge.booqIn.entity.AbstractBookCollection;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record BookUpdate(
        Optional<String> title,
        Optional<Set<String>> isbns,
        Optional<String> description,
        Optional<Date> publicationDate,
        Optional<List<AbstractBookCollection>> collections
) {
}