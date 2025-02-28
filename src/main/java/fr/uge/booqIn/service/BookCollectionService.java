package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.BookCollectionRepository;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookCollectionService {

  private final BookCollectionRepository bookCollectionRepository;
  private final BookRepository bookRepository;

  public BookCollectionService(BookCollectionRepository bookCollectionRepository,
                               BookRepository bookRepository) {
    this.bookCollectionRepository = bookCollectionRepository;
    this.bookRepository = bookRepository;
  }

  @Transactional
  public BookCollection createCollection(BookCollection bookCollection) {
    if (bookCollection.getBooks() == null) {
      bookCollection.setBooks(new ArrayList<>());
    }
    bookCollection.setCreatedAt(new Date());
    bookCollection.setUpdatedAt(new Date());
    return bookCollectionRepository.save(bookCollection);
  }

  @Transactional
  public Optional<BookCollection> findCollectionById(Long id) {
    //return bookCollectionRepository.findById(id);
    return bookCollectionRepository.findByIdWithBooks(id);
  }

  @Transactional
  public Optional<BookCollection> findCollectionByTitle(String title) {
    return bookCollectionRepository.findByTitle(title);
  }

  @Transactional
  public void deleteCollection(Long id) {
    bookCollectionRepository.deleteById(id);
  }

  @Transactional
  public Optional<BookCollection> deleteCollection(Long id, User currentUser) {
    // On récupère l’Optional<BookCollection>
    Optional<BookCollection> optCollection = bookCollectionRepository.findById(id);
    if (optCollection.isPresent()) {
      BookCollection collection = optCollection.get();
      verifyOwnership(collection, currentUser);

      // Retrait de la collection dans chaque Book
      for (Book book : collection.getBooks()) {
        book.getCollections().remove(collection);
      }
      bookCollectionRepository.delete(collection);

      return Optional.of(collection);
    }
    return Optional.empty();
  }

  @Transactional
  public List<BookCollection> getUserCollections(User user) {
    return bookCollectionRepository.findByOwner(user);
  }

  @Transactional
  public Optional<BookCollection> addBookToCollection(Long collectionId, Long bookId, User currentUser) {
    // Vérifie la collection
    Optional<BookCollection> colOpt = bookCollectionRepository.findById(collectionId);
    if (colOpt.isEmpty()) {
      return Optional.empty(); // collection non trouvée
    }
    BookCollection collection = colOpt.get();
    verifyOwnership(collection, currentUser);

    // Vérifie le livre
    Optional<Book> bookOpt = bookRepository.findById(bookId);
    if (bookOpt.isEmpty()) {
      return Optional.empty(); // livre non trouvé
    }

    collection.addBook(bookOpt.get());
    collection.setUpdatedAt(new Date());
    return Optional.of(bookCollectionRepository.save(collection));
  }

  /**
   * Variante qui reçoit un Book déjà existant.
   * Renvoie un Optional vide si la collection n’existe pas.
   * @throws AccessDeniedException si l’utilisateur n’est pas propriétaire.
   */
  @Transactional
  public Optional<BookCollection> addBookToCollection(Long collectionId, Book book, User currentUser) {
    Optional<BookCollection> colOpt = bookCollectionRepository.findById(collectionId);
    if (colOpt.isEmpty()) {
      return Optional.empty();
    }
    BookCollection collection = colOpt.get();
    verifyOwnership(collection, currentUser);

    collection.addBook(book);
    collection.setUpdatedAt(new Date());
    return Optional.of(bookCollectionRepository.save(collection));
  }

  /**
   * Retire un livre de la collection si la collection et le livre existent
   * et si currentUser est propriétaire.
   * @throws AccessDeniedException si ownership fail
   */
  @Transactional
  public Optional<BookCollection> removeBookFromCollection(Long collectionId, Long bookId, User currentUser) {
    Optional<BookCollection> colOpt = bookCollectionRepository.findById(collectionId);
    if (colOpt.isEmpty()) {
      return Optional.empty();
    }
    BookCollection collection = colOpt.get();
    verifyOwnership(collection, currentUser);

    Optional<Book> bookOpt = bookRepository.findById(bookId);
    if (bookOpt.isEmpty()) {
      return Optional.empty();
    }

    collection.removeBook(bookOpt.get());
    collection.setUpdatedAt(new Date());
    return Optional.of(bookCollectionRepository.save(collection));
  }

  @Transactional
  public List<BookCollection> getPublicCollections() {
    return bookCollectionRepository.findByIsPublicTrue();
  }

  /**
   * Met à jour la collection si elle existe et appartient à currentUser.
   * Renvoie un Optional vide si la collection n’existe pas,
   * lève AccessDeniedException si l’utilisateur n’est pas propriétaire.
   */
  @Transactional
  public Optional<BookCollection> updateCollection(Long id, User currentUser, BookCollection updatedCollection) {
    Optional<BookCollection> optCollection = bookCollectionRepository.findById(id);
    if (optCollection.isPresent()) {
      BookCollection collection = optCollection.get();

      // On vérifie l’ownership (403 si fail)
      verifyOwnership(collection, currentUser);

      if (updatedCollection.getTitle() != null) {
        collection.setTitle(updatedCollection.getTitle());
      }
      if (updatedCollection.getDescription() != null) {
        collection.setDescription(updatedCollection.getDescription());
      }
      collection.setPublic(updatedCollection.isPublic());
      collection.setUpdatedAt(new Date());

      bookCollectionRepository.save(collection);
      return Optional.of(collection);
    }
    return Optional.empty();
  }

//  @Transactional
//  public Optional<List<Book>> getAllBooksFromCollection(Long id) {
//    Optional<BookCollection> collection = bookCollectionRepository.findById(id);
//    return collection.map(BookCollection::getBooks);
//  }
  @Transactional
  public List<BookCollection> getUserCollectionsWithBooks(User user) {
    return bookCollectionRepository.findByOwnerWithBooks(user);
  }

  @Transactional
  public Optional<Book> getBookByIdWithCollections(Long id) {
    Optional<Book> bookOpt = bookRepository.findById(id);
    bookOpt.ifPresent(book -> Hibernate.initialize(book.getCollections()));
    return bookOpt;
  }

  @Transactional
  public Optional<Book> getBookFromCollection(Long collectionId, Long bookId) {
    Optional<BookCollection> collection = bookCollectionRepository.findById(collectionId);
    return collection.flatMap(bookCollection -> bookCollection.getBooks().stream()
            .filter(book -> book.getId().equals(bookId))
            .findFirst());
  }

  /**
   * Vérifie que la collection appartient à l'utilisateur, sinon AccessDeniedException.
   */
  private void verifyOwnership(BookCollection collection, User user) {
    //if (!collection.getOwner().equals(user)) {
    if (!collection.getOwner().getUsername().equals(user.getUsername())) {
      throw new AccessDeniedException("You don't have permission to modify this collection");
    }
  }
}

