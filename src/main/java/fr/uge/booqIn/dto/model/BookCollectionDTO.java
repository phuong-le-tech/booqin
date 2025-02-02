package fr.uge.booqIn.dto.model;

import java.util.List;

public record BookCollectionDTO(
        Long id,
        String title,
        String description,
        UserDTO owner,
        boolean isPublic,
        List<Long> bookIds,
        String createdAt,
        String updatedAt
) { }
