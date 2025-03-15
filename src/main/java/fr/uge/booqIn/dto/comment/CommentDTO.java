package fr.uge.booqIn.dto.comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CommentDTO(
        Long id,
        String content,
        Long userId,
        String username,
        Long entityId,
        String entityType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentDTO> replies
) {

    public CommentDTO {
        if (replies == null) {
            replies = new ArrayList<>();
        }
    }


    public CommentDTO(
            Long id,
            String content,
            Long userId,
            String username,
            Long entityId,
            String entityType,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this(id, content, userId, username, entityId, entityType, createdAt, updatedAt, new ArrayList<>());
    }
}