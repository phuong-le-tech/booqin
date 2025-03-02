package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.entity.Book;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record BookCollectionUpdate(
        Optional<String> name,
        Optional<String> description,
        Optional<List<Book>> books
) {}
