package fr.uge.booqIn.strategy;

import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.interfaces.PolymorphicEntity;
import fr.uge.booqIn.strategy.handlers.EntityHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EntityStrategyContext {
    private final List<EntityHandler<? extends PolymorphicEntity>> handlers;
    private final Map<String, EntityHandler<? extends PolymorphicEntity>> handlerMap = new ConcurrentHashMap<>();

    public EntityStrategyContext(List<EntityHandler<? extends PolymorphicEntity>> handlers) {
        this.handlers = handlers;
    }

    @PostConstruct
    public void initializeHandlers() {
        handlers.forEach(handler -> {
            handlerMap.put(handler.getEntityType().toUpperCase(), handler);
        });
    }


    public PolymorphicEntity findByTypeAndId(String type, Long id) {
        return getHandler(type)
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(type, "id", id));
    }

    @SuppressWarnings("unchecked")
    public <T extends PolymorphicEntity> EntityHandler<T> getHandler(String type) {
        Objects.requireNonNull(type);

        EntityHandler<? extends PolymorphicEntity> handler = handlerMap.get(type.toUpperCase());

        if (handler == null) {
            handler = handlers.stream()
                    .filter(h -> h.canHandle(type))
                    .findFirst()
                    .orElse(null);
        }

        if (handler == null) {
            throw new IllegalArgumentException("No handler found for entity type: " + type);
        }

        return (EntityHandler<T>) handler;
    }
}
