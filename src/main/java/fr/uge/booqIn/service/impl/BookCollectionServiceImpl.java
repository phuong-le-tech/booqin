package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.specification.BookCollectionSpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class BookCollectionServiceImpl implements BookCollectionServiceInterface {
  private static final Logger logger = Logger.getLogger(BookCollectionServiceImpl.class.getName());
  private final BookCollectionRepository bookCollectionRepository;
  private final BookRepository bookRepository;
  private final BookCollectionMapper bookCollectionMapper;

  public BookCollectionServiceImpl(BookCollectionRepository bookCollectionRepository,
                                   BookRepository bookRepository,
                                   BookCollectionMapper bookCollectionMapper) {
    this.bookCollectionRepository = bookCollectionRepository;
    this.bookRepository = bookRepository;
    this.bookCollectionMapper = bookCollectionMapper;
  }

  @Override
  @Transactional
  public Page<BookCollectionIndex> findBookCollections(String name, String description, Long userId, Pageable pageable) {
    // Build the specification for filtering
    Specification<BookCollection> spec = Specification.where(null);

    if (name != null && !name.isEmpty()) {
      spec = spec.and(BookCollectionSpecification.hasTitle(name));
    }

    if (description != null && !description.isEmpty()) {
      spec = spec.and(BookCollectionSpecification.hasDescription(description));
    }

    if (userId != null) {
      spec = spec.and(BookCollectionSpecification.hasOwner(userId));
    }

    return bookCollectionRepository.findAll(spec, pageable)
            .map(bookCollectionMapper::toBookCollectionIndex);
  }

  @Override
  @Transactional
  public BookCollectionShow findById(@NotNull Long id) {
    BookCollection bookCollection = findBookCollectionById(id);
    return bookCollectionMapper.toBookCollectionShow(bookCollection);
  }

  @Override
  @Transactional
  public BookCollection createBookCollection(@NotNull @Valid BookCollection bookCollection) {
    validateBooksExist(bookCollection.getBooks());

    // Ensure books collection is initialized
    if (bookCollection.getBooks() == null) {
      bookCollection.setBooks(new ArrayList<>());
    }

    bookCollection.setCreatedAt(new Date());
    bookCollection.setUpdatedAt(new Date());
    return bookCollectionRepository.save(bookCollection);
  }

  @Override
  @Transactional
  public BookCollection updateBookCollection(@NotNull @Valid BookCollection bookCollection) {
    // Verify collection exists
    if (!bookCollectionRepository.existsById(bookCollection.getId())) {
      throw new ResourceNotFoundException("BookCollection", "id", bookCollection.getId());
    }
    verifyOwnership(bookCollection);
    validateBooksExist(bookCollection.getBooks());

    bookCollection.setUpdatedAt(new Date());
    return bookCollectionRepository.save(bookCollection);
  }

  @Override
  @Transactional
  public BookCollectionShow patchBookCollection(@NotNull Long id, @NotNull @Valid BookCollectionUpdate bookCollectionUpdate) {
    BookCollection bookCollection = findBookCollectionById(id);

    bookCollectionUpdate.name().ifPresent(bookCollection::setTitle);
    bookCollectionUpdate.description().ifPresent(bookCollection::setDescription);

    bookCollectionUpdate.books().ifPresent(books -> {
      validateBooksExist(books);
      bookCollection.setBooks(books);
    });

    bookCollection.setUpdatedAt(new Date());
    return bookCollectionMapper.toBookCollectionShow(bookCollectionRepository.save(bookCollection));
  }

  @Override
  @Transactional
  public void deleteBookCollection(@NotNull Long id) {
    User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    BookCollection bookCollection = findBookCollectionById(id);
    verifyOwnership(bookCollection);

    bookCollectionRepository.delete(bookCollection);
  }

  @Override
  @Transactional
  public BookCollection addBookToCollection(@NotNull Long collectionId, @NotNull Long bookId) {
    // Find the collection
    BookCollection collection = findBookCollectionById(collectionId);

    // Verify ownership
    verifyOwnership(collection);

    // Find the book
    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

    // Add the book to collection
    collection.addBook(book);
    collection.setUpdatedAt(new Date());

    return bookCollectionRepository.save(collection);
  }

  @Transactional
  public List<BookCollection> getUserCollections(User user) {
    return bookCollectionRepository.findByOwner(user);
  }

  @Transactional
  public List<BookCollection> getUserCollectionsWithBooks(User user) {
    return bookCollectionRepository.findByOwnerWithBooks(user);
  }

  @Transactional
  public List<BookCollection> getPublicCollections() {
    return bookCollectionRepository.findByIsPublicTrue();
  }

  @Transactional
  public Book getBookFromCollection(Long collectionId, Long bookId) {
    BookCollection collection = findBookCollectionById(collectionId);
    return collection.getBooks().stream().filter(b -> b.getId().equals(bookId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
  }

  @Transactional
  public BookCollection removeBookFromCollection(Long collectionId, Long bookId, User currentUser) {
    BookCollection collection = findBookCollectionById(collectionId);

    verifyOwnership(collection);

    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

    collection.removeBook(book);
    collection.setUpdatedAt(new Date());

    return bookCollectionRepository.save(collection);
  }

  /**
   * Helper methods
   */

  private BookCollection findBookCollectionById(Long id) {
    return bookCollectionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("BookCollection", "id", id));
  }

  private void verifyOwnership(BookCollection collection) {
    User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!collection.getOwner().getUsername().equals(currentUser.getUsername())) {
      throw new AccessDeniedException("You don't have permission to modify this collection");
    }
  }

  private void validateBooksExist(List<Book> books) {
    if (books != null) {
      books.forEach(book -> {
        if (book.getId() != null && !bookRepository.existsById(book.getId())) {
          throw new ResourceNotFoundException("Book", "id", book.getId());
        }
      });
    }
  }
}