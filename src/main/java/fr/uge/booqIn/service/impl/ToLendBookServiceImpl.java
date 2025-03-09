package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.entity.ToLendBook;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.repository.ToLendBookRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.ToLendBookServiceInterface;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ToLendBookServiceImpl implements ToLendBookServiceInterface {
  
  private final ToLendBookRepository toLendBookRepository;
  private final UserRepository userRepository ;
  
  public ToLendBookServiceImpl(ToLendBookRepository toLendBookRepository, UserRepository userRepository) {
    this.toLendBookRepository = toLendBookRepository;
    this.userRepository = userRepository;
  }
  
  public boolean reserveBook(Long bookId, Long userId) {
    Optional<ToLendBook> toLendBookOpt = toLendBookRepository.findById(bookId);
    Optional<User> userOpt = userRepository.findById(userId);
    
    if (toLendBookOpt.isEmpty() || userOpt.isEmpty()) {
      return false;
    }
    
    ToLendBook toLendBook = toLendBookOpt.get();
    User user = userOpt.get();
    
    if (toLendBook.getStatus() != BookStatus.AVAILABLE) {
      return false;
    }
    toLendBook.setBorrower(user);
    toLendBook.setReservedAt(LocalDateTime.now());
    toLendBook.setStatus(BookStatus.RESERVED);
    toLendBookRepository.save(toLendBook);
    
    return true;
  }
  
  
  @Override
  @Scheduled(fixedRate = 60000)
  public void expireReservations() {
    LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(20);
    List<ToLendBook> expiredReservations = toLendBookRepository.findByStatusAndReservedAtBefore(BookStatus.RESERVED, expirationTime);
    
    for (ToLendBook toLendBook : expiredReservations) {
      toLendBook.setStatus(BookStatus.AVAILABLE);
      toLendBook.setBorrower(null);
      toLendBook.setReservedAt(null);
      toLendBookRepository.save(toLendBook);
    }
  }
}
