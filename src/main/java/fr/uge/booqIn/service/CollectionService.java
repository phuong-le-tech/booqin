package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.CollectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CollectionService {
  private final CollectionRepository collectionRepository;
  private final BookRepository bookRepository;

  public CollectionService(CollectionRepository collectionRepository, BookRepository bookRepository) {
    this.collectionRepository = collectionRepository;
    this.bookRepository = bookRepository;
  }

  public BookCollection createCollection(BookCollection bookCollection) {
    if (bookCollection.getBooks() == null) {
      bookCollection.setBooks(new ArrayList<>());
    }
    bookCollection.setCreatedAt(new Date());
    bookCollection.setUpdatedAt(new Date());
    return collectionRepository.save(bookCollection);
  }

  @Transactional
  public BookCollection getCollectionById(Long id) {
    return collectionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
  }

  @Transactional
  public BookCollection getCollectionByTitle(String title) {
    return collectionRepository.findByTitle(title)
            .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
  }

  public void deleteCollection(Long id) {
    collectionRepository.deleteById(id);
  }

  @Transactional
  public void deleteCollection(Long id, User currentUser) {
    var collection = getCollectionById(id);
    verifyOwnership(collection, currentUser);
    for (Book book : collection.getBooks()) {
      book.getCollections().remove(collection);
    }
    collectionRepository.delete(collection);
  }

  @Transactional
  public List<BookCollection> getUserCollections(User user) {
    return collectionRepository.findByOwner(user);
  }

  @Transactional
  public BookCollection addBookToCollection(Long collectionId, Long bookId, User currentUser) {
    var collection = getCollectionById(collectionId);
    verifyOwnership(collection, currentUser);
    var book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found"));

    collection.addBook(book);
    collection.setUpdatedAt(new Date());
    return collectionRepository.save(collection);
  }

  @Transactional
  public BookCollection addBookToCollection(Long collectionId, Book book, User currentUser) {
    var collection = getCollectionById(collectionId);
    verifyOwnership(collection, currentUser);
    collection.addBook(book);
    collection.setUpdatedAt(new Date());
    return collectionRepository.save(collection);
  }

  @Transactional
  public BookCollection removeBookFromCollection(Long collectionId, Long bookId, User currentUser) {
    var collection = getCollectionById(collectionId);
    verifyOwnership(collection, currentUser);
    var book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found"));

    collection.removeBook(book);
    collection.setUpdatedAt(new Date());
    return collectionRepository.save(collection);
  }

  private void verifyOwnership(BookCollection collection, User user) {
    if (!collection.getOwner().equals(user)) {
      throw new AccessDeniedException("You don't have permission to modify this collection");
    }
  }
}
