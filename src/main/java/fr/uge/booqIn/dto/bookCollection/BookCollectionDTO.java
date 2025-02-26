package fr.uge.booqIn.dto.bookCollection;

import fr.uge.booqIn.dto.user.UserDto;

import java.util.List;

public record BookCollectionDTO(
        Long id,
        String title,
        String description,
        UserDto owner,
        boolean isPublic,
        List<Long> bookIds,
        String createdAt,
        String updatedAt
) { }
