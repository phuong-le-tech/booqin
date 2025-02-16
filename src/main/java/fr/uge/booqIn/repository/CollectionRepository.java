package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<BookCollection, Long> {
  Optional<BookCollection> findByTitle(String title);
  List<BookCollection> findByOwner(User owner);
  List<BookCollection> findByIsPublicTrue();
}
