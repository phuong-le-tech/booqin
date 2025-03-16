package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookNotificationQueue;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class NotificationMappeur {

public BookNotificationDTO mapToBookNotificationDTO(BookNotificationQueue notification) {
  Book book = notification.getBook();
  return BookNotificationDTO.builder()
          .id(notification.getId())
          .bookId(book.getId())
          .bookTitle(book.getTitle())
          .authorNames(getAuthorNames(book))
          .status(notification.getStatus())
          .expiresAt(notification.getReservationExpiresAt())
          .position(notification.getPosition())
          .createdAt(notification.getCreatedAt())
          .build();
  }
  
  private String getAuthorNames(Book book) {
    if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
      return "Auteur inconnu";
    }
    
    return book.getAuthors().stream()
            .map(author -> author.getFirstName() + " " + author.getLastName())
            .collect(Collectors.joining(", "));
  }
  
}
