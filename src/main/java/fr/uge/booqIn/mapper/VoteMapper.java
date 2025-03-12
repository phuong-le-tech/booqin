package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteResponse;
import fr.uge.booqIn.entity.Vote;
import org.springframework.stereotype.Component;

@Component
public class VoteMapper {


    public VoteResponse toVoteResponse(Vote vote) {
        return new VoteResponse(
            vote.getId(),
            vote.getUser().getId(),
            vote.getUser().getUsername(),
            vote.getEntityId(),
            vote.getEntityType(),
            vote.getVoteType()
        );
    }

    public VoteCount toVoteCount(Long entityId, String entityType, long upvoteCount, long downvoteCount, int score) {
        return new VoteCount(
            entityId,
            entityType,
            upvoteCount,
            downvoteCount,
            score
        );
    }
}