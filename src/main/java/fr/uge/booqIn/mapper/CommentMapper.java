package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.entity.Comment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public CommentDTO toDTO(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getEntityId(),
                comment.getEntityType(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                Collections.emptyList() // Les réponses seront chargées séparément si nécessaire
        );
    }

    @Transactional(readOnly = true)
    public CommentDTO toTreeDTO(Comment comment) {
        if (comment == null) {
            return null;
        }

        List<CommentDTO> replyDTOs = Collections.emptyList();

        // Safely handle replies - only process if the collection is initialized and not empty
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            replyDTOs = comment.getReplies().stream()
                    .map(this::toTreeDTO)
                    .collect(Collectors.toList());
        }

        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getEntityId(),
                comment.getEntityType(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replyDTOs
        );
    }

    public List<CommentDTO> toDTOList(List<Comment> comments) {
        if (comments == null) {
            return Collections.emptyList();
        }

        return comments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> toTreeDTOList(List<Comment> comments) {
        if (comments == null) {
            return Collections.emptyList();
        }

        return comments.stream()
                .map(this::toTreeDTO)
                .collect(Collectors.toList());
    }
}