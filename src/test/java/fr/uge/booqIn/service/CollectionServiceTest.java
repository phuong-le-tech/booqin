package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.CollectionRepository;
import fr.uge.booqIn.service.CollectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CollectionServiceTest {
  @Mock
  private CollectionRepository collectionRepository;

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private CollectionService collectionService;

  @Test
  void testBookInitialization() {
    Book book = new Book();
    assertNotNull(book.getCollections(), "La liste collections ne doit pas être null");
  }

  @Test
  void testAddBookToCollection() {
    User user = new User();
    BookCollection collection = new BookCollection("My Collection", "Description", user);
    Book book = new Book();
    book.setId(1L);

    when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(collectionRepository.save(any(BookCollection.class))).thenReturn(collection);

    BookCollection updatedCollection = collectionService.addBookToCollection(1L, 1L, user);
    assertNotNull(updatedCollection, "La collection mise à jour ne doit pas être null"); // ✅ Vérification
    assertTrue(updatedCollection.getBooks().contains(book));
  }
}
