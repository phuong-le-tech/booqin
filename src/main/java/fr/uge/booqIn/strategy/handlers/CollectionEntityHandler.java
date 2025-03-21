package fr.uge.booqIn.strategy.handlers;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.repository.BookCollectionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CollectionEntityHandler implements EntityHandler<AbstractBookCollection> {

    private final BookCollectionRepository collectionRepository;

    public CollectionEntityHandler(BookCollectionRepository bookcollectionRepository) {
        this.collectionRepository = bookcollectionRepository;
    }

    @Override
    public String getEntityType() {
        return "COLLECTION";
    }

    @Override
    public Optional<AbstractBookCollection> findById(Long id) {
        return collectionRepository.findById(id);
    }
}