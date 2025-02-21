package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
}
