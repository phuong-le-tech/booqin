package fr.uge.booqIn.strategy.handlers;

import fr.uge.booqIn.model.PolymorphicEntity;

import java.util.Optional;

public interface EntityHandler<T extends PolymorphicEntity> {
    String getEntityType();
    Optional<T> findById(Long id);
    default boolean canHandle(String entityType) {
        return getEntityType().equalsIgnoreCase(entityType);
    }
}