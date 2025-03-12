package fr.uge.booqIn.strategy;

import fr.uge.booqIn.entity.Votable;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VotableStrategyContext {
    private final List<VotableHandler<? extends Votable>> handlers;
    private final Map<String, VotableHandler<? extends Votable>> handlerMap = new ConcurrentHashMap<>();

    public VotableStrategyContext(List<VotableHandler<? extends Votable>> handlers) {
        this.handlers = handlers;
    }

    @PostConstruct
    public void initializeHandlers() {
        handlers.forEach(handler -> {
            handlerMap.put(handler.getVotableType().toUpperCase(), handler);
        });
    }

    public Votable findByTypeAndId(String type, Long id) {
        return getHandler(type)
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(type, "id", id));
    }

    public boolean exists(String type, Long id) {
        try {
            VotableHandler<? extends Votable> handler = getHandler(type);
            return handler.findById(id).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Votable> VotableHandler<T> getHandler(String type) {
        Objects.requireNonNull(type);

        VotableHandler<? extends Votable> handler = handlerMap.get(type.toUpperCase());

        if (handler == null) {
            handler = handlers.stream()
                    .filter(h -> h.canHandle(type))
                    .findFirst()
                    .orElse(null);
        }

        if (handler == null) {
            throw new IllegalArgumentException("No handler found for votable type: " + type);
        }

        return (VotableHandler<T>) handler;
    }
}