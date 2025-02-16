package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.BookCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookCollectionRepository extends JpaRepository<BookCollection, Long> {

    Optional<BookCollection> findById(Long id);

}
