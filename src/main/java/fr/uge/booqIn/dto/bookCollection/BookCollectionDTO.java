package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.dto.user.UserDTO;

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
