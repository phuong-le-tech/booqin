package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.BookCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookCollectionRepository extends JpaRepository<BookCollection,Long> {
    Optional<BookCollection> findById(Long id);

}
