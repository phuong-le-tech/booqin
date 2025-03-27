package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.service.interfaces.VoteServiceInterface;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserVoteService {
    private final VoteServiceInterface voteService;

    public UserVoteService(VoteServiceInterface voteService) {
        this.voteService = voteService;
    }

    /**
     * Collecte les votes utilisateur pour une liste d'entités d'un type spécifique
     */
    public Map<Long, VoteType> getUserVotesForEntities(Long userId, List<Long> entityIds, String entityType) {
        if (userId == null || entityIds == null || entityIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, VoteType> userVotes = new HashMap<>();

        for (Long entityId : entityIds) {
            VoteType voteType = voteService.getUserVoteType(userId, entityId, entityType);
            if (voteType != null) {
                userVotes.put(entityId, voteType);
            }
        }

        return userVotes;
    }

    /**
     * Collecte récursivement les votes sur les commentaires
     */
    public Map<Long, VoteType> collectCommentVotes(Long userId, List<CommentDTO> comments) {
        Map<Long, VoteType> votes = new HashMap<>();

        collectCommentVotesRecursive(userId, comments, votes);

        return votes;
    }

    private void collectCommentVotesRecursive(Long userId, List<CommentDTO> comments, Map<Long, VoteType> votes) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        for (CommentDTO comment : comments) {
            VoteType voteType = voteService.getUserVoteType(userId, comment.id(), "COMMENT");
            if (voteType != null) {
                votes.put(comment.id(), voteType);
            }

            // Récursion pour les réponses
            if (comment.replies() != null && !comment.replies().isEmpty()) {
                collectCommentVotesRecursive(userId, comment.replies(), votes);
            }
        }
    }
}