package fr.uge.booqIn.dto.bookCollection;

import java.util.Optional;

public record BookCollectionUpdate(
        Optional<String> title,
        Optional<String> description,
        boolean isPublic
) {}
