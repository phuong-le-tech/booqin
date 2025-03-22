package fr.uge.booqIn.dto.follow;

import java.time.LocalDateTime;

public record FollowDto(
        Long id,
        Long followerId,
        Long entityId,
        String entityType,
        LocalDateTime createdAt
) {
}

