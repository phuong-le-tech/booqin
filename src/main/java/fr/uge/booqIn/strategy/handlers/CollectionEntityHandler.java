package fr.uge.booqIn.strategy.handlers;

import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.repository.BookCollectionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CollectionEntityHandler implements EntityHandler<BookCollection> {

    private final BookCollectionRepository collectionRepository;

    public CollectionEntityHandler(BookCollectionRepository bookcollectionRepository) {
        this.collectionRepository = bookcollectionRepository;
    }

    @Override
    public String getEntityType() {
        return "COLLECTION";
    }

    @Override
    public Optional<BookCollection> findById(Long id) {
        return collectionRepository.findById(id);
    }
}