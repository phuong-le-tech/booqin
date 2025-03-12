package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.entity.Book;

import java.util.List;
import java.util.Optional;

public record BookCollectionUpdate(
        Optional<String> name,
        Optional<String> description,
        boolean isPublic,
        Optional<List<Book>> books
) {}
