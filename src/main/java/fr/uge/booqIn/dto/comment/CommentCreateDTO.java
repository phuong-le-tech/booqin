package fr.uge.booqIn.dto.comment;

public record CommentCreateDTO(
    Long userId,
    Long entityId,
    String entityType,
    String content,
    Long parentId
) {}