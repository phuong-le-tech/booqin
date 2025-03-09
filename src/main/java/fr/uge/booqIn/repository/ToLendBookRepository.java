package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.ToLendBook;
import fr.uge.booqIn.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ToLendBookRepository extends JpaRepository<ToLendBook, Long> {
  List<ToLendBook> findByStatusAndReservedAtBefore(BookStatus status, LocalDateTime createdAt);
}
