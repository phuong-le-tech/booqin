package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.BookCollection;

import java.util.Optional;

public interface BookCollectionRepository {
    Optional<BookCollection> findById(Long id);

}
