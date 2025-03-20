package fr.uge.booqIn.dto.bookCollection;

import java.util.Map;
import java.util.Optional;


public record BookCollectionCreate(
         String title,
         String description,
         boolean isPublic,
         Optional<Map<String, Object>> criteria
) {}
