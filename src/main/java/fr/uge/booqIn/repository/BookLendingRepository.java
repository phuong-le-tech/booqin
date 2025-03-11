package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookLendingRepository extends JpaRepository<BookLending, Long> {
  Optional<BookLending> findById(Long id);
  Optional<BookLending> findByBookId(Long bookId);
  List<BookLending> findByStatusAndReservedAtBefore(BookStatus status, LocalDateTime expirationTime);
  Optional<BookLending> findByBookAndBorrower(Book book  , User user);
  List<BookLending> findByBorrower(User user);
}
