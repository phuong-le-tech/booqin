package fr.uge.booqIn.strategy;

import fr.uge.booqIn.entity.Votable;

import java.util.Optional;

public interface VotableHandler<T extends Votable> {
    String getVotableType();
    Optional<T> findById(Long id);
    default boolean canHandle(String votableType) {
        return getVotableType().equals(votableType);
    }
}
