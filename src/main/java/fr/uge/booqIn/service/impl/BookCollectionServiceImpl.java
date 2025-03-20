package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.bookCollection.*;
import fr.uge.booqIn.entity.*;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.model.BookCollection;
import fr.uge.booqIn.repository.*;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.specification.BookCollectionSpecification;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;

@Service
public class BookCollectionServiceImpl implements BookCollectionServiceInterface {
  private final BookCollectionRepository bookCollectionRepository;
  private final BookRepository bookRepo;
  private final BookCollectionMapper bookCollectionMapper;


  public BookCollectionServiceImpl(BookCollectionRepository bookCollectionRepository,
                                   BookRepository bookRepo,
                                   BookCollectionMapper bookCollectionMapper) {
    this.bookCollectionRepository = bookCollectionRepository;
    this.bookRepo = bookRepo;
    this.bookCollectionMapper = bookCollectionMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<BookCollectionIndex> findBookCollections(String name, String description, Long userId, Pageable pageable) {
    Specification<AbstractBookCollection> spec = Specification.where(null);

    if (name != null && !name.isEmpty()) {
      spec = spec.and(BookCollectionSpecification.hasTitle(name));
    }

    if (description != null && !description.isEmpty()) {
      spec = spec.and(BookCollectionSpecification.hasDescription(description));
    }

    if (userId != null) {
      spec = spec.and(BookCollectionSpecification.hasOwner(userId));
    }

    System.out.println("spec: ");
    return bookCollectionRepository.findAll(spec, pageable)
            .map(bookCollectionMapper::toBookCollectionIndex);
  }

  @Override
  @Transactional(readOnly = true)
  public BookCollectionShow findById(Long id) throws ResourceNotFoundException {
    return bookCollectionRepository.findById(id)
            .map(bookCollectionMapper::toBookCollectionShow)
            .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
  }

  @Override
  @Transactional
  public BookCollectionShow createBookCollection(BookCollectionCreate collection, User currentUser) {
    if (collection.criteria().isEmpty()) {
      NormalBookCollection newCollection = new NormalBookCollection.Builder()
              .title(collection.title())
              .description(collection.description())
              .owner(currentUser)
              .isPublic(collection.isPublic())
              .build();

      var savedCollection = bookCollectionRepository.save(newCollection);
      return bookCollectionMapper.toBookCollectionShow(savedCollection);
    } else {
      SmartBookCollection newCollection = new SmartBookCollection.Builder()
              .title(collection.title())
              .description(collection.description())
              .owner(currentUser)
              .isPublic(collection.isPublic())
              .criteria(collection.criteria().get().toString())
              .build();

      var savedCollection = bookCollectionRepository.save(newCollection);
      return bookCollectionMapper.toBookCollectionShow(savedCollection);
    }
  }

  @Override
  @Transactional
  public BookCollectionShow updateBookCollection(Long id, BookCollectionUpdate collection) {
    AbstractBookCollection existingCollection = bookCollectionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

    if (collection.title().isPresent()) {
      existingCollection.setTitle(collection.title().get());
    }
    if (collection.description().isPresent()) {
      existingCollection.setDescription(collection.description().get());
    }
    existingCollection.setPublic(collection.isPublic());

    AbstractBookCollection updatedCollection = bookCollectionRepository.save(existingCollection);
    return bookCollectionMapper.toBookCollectionShow(updatedCollection);
  }

  @Override
  public void deleteBookCollection(Long id) throws ResourceNotFoundException {
    AbstractBookCollection collection = bookCollectionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
    bookCollectionRepository.delete(collection);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookCollectionShow> getUserCollections(User user) {
    return bookCollectionRepository.findByOwner(user)
            .stream()
            .map(bookCollectionMapper::toBookCollectionShow)
            .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookCollectionShow> getPublicCollections() {
    return bookCollectionRepository.findByIsPublicTrue()
            .stream()
            .map(bookCollectionMapper::toBookCollectionShow)
            .toList();

  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void addBookToCollection(@NotNull Long collectionId, @NotNull Long bookId) {
    BookCollection collection = findBookCollectionById(collectionId);

    Optional<Book> book = bookRepo.findById(bookId);
    if (book.isEmpty()) {
      throw new ResourceNotFoundException("Book", "id", bookId);
    }
    collection.addBook(book.get());
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void removeBookFromCollection(Long collectionId, Long bookId) {
    BookCollection collection = findBookCollectionById(collectionId);
    Optional<Book> book = bookRepo.findById(bookId);
    if (book.isEmpty()) {
      throw new ResourceNotFoundException("Book", "id", bookId);
    }

    collection.removeBook(book.get());
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookCollectionShow> getUserCollectionsWithBooks(User user) {
    return bookCollectionRepository.findByOwnerWithBooks(user)
            .stream().map(bookCollectionMapper::toBookCollectionShow)
            .toList();
  }


  /**
   * Helper methods
   */
  private BookCollection findBookCollectionById(Long id) {
    return bookCollectionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("BookCollection", "id", id));
  }

}
