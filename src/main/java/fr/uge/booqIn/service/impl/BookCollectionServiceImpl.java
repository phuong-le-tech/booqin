package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.bookCollection.*;
import fr.uge.booqIn.entity.*;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.factory.BookCollectionFactory;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.interfaces.BookCollection;
import fr.uge.booqIn.repository.*;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.specification.BookCollectionSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class BookCollectionServiceImpl implements BookCollectionServiceInterface {
  private static final Logger logger = Logger.getLogger(BookCollectionServiceImpl.class.getName());
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
  public Page<BookCollectionIndex> findBookCollections(String title,
                                                       String description,
                                                       boolean isPublic,
                                                       @NonNull Pageable pageable) {
    Specification<AbstractBookCollection> spec = Specification.where(null);

    if (title != null && !title.isEmpty()) {
      spec = spec.and(BookCollectionSpecification.hasTitle(title));
    }

    if (description != null && !description.isEmpty()) {
      spec = spec.and(BookCollectionSpecification.hasDescription(description));
    }

    if (isPublic) {
      spec = spec.and(BookCollectionSpecification.isPublic());
    }

    return bookCollectionRepository.findAll(spec, pageable)
            .map(bookCollectionMapper::toBookCollectionIndex);
  }

  @Override
  @Transactional(readOnly = true)
  public BookCollectionShow findBookCollectionById(@NonNull Long id) throws ResourceNotFoundException {
    return bookCollectionRepository.findById(id)
            .map(bookCollectionMapper::toBookCollectionShow)
            .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class, ConcurrentModificationException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public BookCollectionShow createBookCollection(@NonNull @Valid BookCollectionCreate collection,
                                                 @NonNull User currentUser) {
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
      SmartBookCollection smartCollection = BookCollectionFactory.createSmartCollection(
              collection.title(),
              collection.description(),
              collection.isPublic(),
              collection.criteria().get(),
              currentUser,
              bookRepo.findAll()
      );
      var newCollection = bookCollectionRepository.save(smartCollection);
      return bookCollectionMapper.toBookCollectionShow(newCollection);
    }
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class, ConcurrentModificationException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public BookCollectionShow updateBookCollection(@NonNull Long id,
                                                 @NonNull @Valid BookCollectionUpdate collection) {
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
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class, ConcurrentModificationException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void deleteBookCollection(@NonNull Long id) throws ResourceNotFoundException {
    AbstractBookCollection collection = bookCollectionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
    bookCollectionRepository.delete(collection);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<BookCollectionIndex> getPublicCollections(@NonNull Pageable pageable) {
    Page<AbstractBookCollection> collectionsPage = bookCollectionRepository.findByIsPublicTrue(pageable);
    return collectionsPage.map(bookCollectionMapper::toBookCollectionIndex);
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class, ConcurrentModificationException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void addBookToCollection(@NonNull Long collectionId, @NonNull Long bookId) {
    AbstractBookCollection collection = findById(collectionId);
    Optional<Book> book = bookRepo.findById(bookId);
    if (book.isEmpty()) {
      throw new ResourceNotFoundException("Book", "id", bookId);
    }
    collection.addBook(book.get());
    bookCollectionRepository.save(collection);
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class, ConcurrentModificationException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void removeBookFromCollection(@NonNull Long collectionId, @NonNull Long bookId) {
    AbstractBookCollection collection = findById(collectionId);
    Optional<Book> book = bookRepo.findById(bookId);
    if (book.isEmpty()) {
      throw new ResourceNotFoundException("Book", "id", bookId);
    }
    collection.removeBook(book.get());
    bookCollectionRepository.save(collection);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<BookCollectionShow> getUserCollectionsWithBooks(@NonNull User user, @NonNull Pageable pageable) {
    List<AbstractBookCollection> collections = bookCollectionRepository.findByOwnerWithBooks(user);

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), collections.size());

    List<AbstractBookCollection> pagedList = collections.subList(start, end);
    List<BookCollectionShow> result = pagedList.stream()
            .map(bookCollectionMapper::toBookCollectionShow)
            .toList();

    return new PageImpl<>(result, pageable, collections.size());
  }
  
  public  <T> Page<T> createPaginatedResult(List<T> list, Pageable pageable) {
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), list.size());
    
    if (start > list.size()) {
      return new PageImpl<>(Collections.emptyList(), pageable, list.size());
    }
    
    List<T> subList = list.subList(start, end);
    return new PageImpl<>(subList, pageable, list.size());
  }
  
  @Override
  @Transactional(readOnly = true)
  public BookCollection findCollectionById(@NonNull Long id) {
    return findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<BookCollectionIndex> getPublicUserCollection(@NonNull User owner, @NonNull Pageable pageable) {
    Page<AbstractBookCollection> collectionsPage = bookCollectionRepository.findPublicCollectionsByOwner(owner, pageable);
    return collectionsPage.map(bookCollectionMapper::toBookCollectionIndex);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<BookCollectionIndex> getPrivateUserCollection(@NonNull User owner, @NonNull Pageable pageable) {
    Page<AbstractBookCollection> collectionsPage = bookCollectionRepository.findPrivateCollectionsByOwner(owner, pageable);
    return collectionsPage.map(bookCollectionMapper::toBookCollectionIndex);
  }

  @Override
  @Transactional(readOnly = true)
  public  List<AbstractBookCollection> findPublicCollectionsByOwnerId(@NonNull Long ownerId) {
    return bookCollectionRepository.findPublicCollectionsByOwnerId(ownerId);
  }

  @Override
  @Transactional(readOnly = true)
  public AbstractBookCollection findLikedCollectionByOwnerId(@NonNull Long ownerId) {
    return bookCollectionRepository.findLikedCollectionByOwnerId(ownerId)
            .orElseThrow( () -> new ResourceNotFoundException("Liked collection not found for user: " + ownerId));
  }
  /**
   * Helper methods
   */
  private AbstractBookCollection findById(Long id) {
    AbstractBookCollection collectionWithBooks = bookCollectionRepository.findByIdWithBooks(id)
            .orElseThrow(() -> new ResourceNotFoundException("BookCollection", "id", id));
    collectionWithBooks.getOwner().getFullName();
    return collectionWithBooks;
  }

  // Recovery methods for retryable operations
  @Recover
  public BookCollectionShow recoverFromOptimisticLockingFailureCreate(
          ObjectOptimisticLockingFailureException ex,
          BookCollectionCreate collection,
          User currentUser) {
    logger.warning("Concurrent modification detected while creating collection: " + collection.title());
    throw new ConcurrentModificationException("The collection has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public BookCollectionShow recoverFromOptimisticLockingFailureUpdate(
          ObjectOptimisticLockingFailureException ex, @NonNull Long id, @NonNull @Valid BookCollectionUpdate collection) {
    logger.warning("Concurrent modification detected while updating collection with ID: " + id);
    throw new ConcurrentModificationException("The collection has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureDelete(
          ObjectOptimisticLockingFailureException ex, @NonNull Long id) {
    logger.warning("Concurrent modification detected while deleting collection with ID: " + id);
    throw new ConcurrentModificationException("The collection has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureAddBook(
          ObjectOptimisticLockingFailureException ex, @NonNull Long collectionId, @NonNull Long bookId) {
    logger.warning("Concurrent modification detected while adding book ID: " + bookId + " to collection ID: " + collectionId);
    throw new ConcurrentModificationException("The collection has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureRemoveBook(
          ObjectOptimisticLockingFailureException ex, @NonNull Long collectionId, @NonNull Long bookId) {
    logger.warning("Concurrent modification detected while removing book ID: " + bookId + " from collection ID: " + collectionId);
    throw new ConcurrentModificationException("The collection has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public BookCollectionShow recoverFromConcurrentModificationFailureCreate(
          ConcurrentModificationException ex,
          BookCollectionCreate collection,
          User currentUser) {
    logger.warning("Concurrent modification detected while creating collection: " + collection.title());
    throw new ConcurrentModificationException("The collection has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public BookCollectionShow recoverFromAllExceptions(
          Exception ex, @NonNull @Valid BookCollectionCreate collection, @NonNull User currentUser) {
    logger.warning("Échec après plusieurs tentatives lors de la création de la collection: " + collection.title() + ", erreur: " + ex.getMessage());
    throw new RuntimeException("Impossible de créer la collection: " + ex.getMessage(), ex);
  }

  @Recover
  public BookCollectionShow recoverFromConcurrentModificationFailureUpdate(
          ConcurrentModificationException ex, @NonNull Long id, @NonNull @Valid BookCollectionUpdate collection) {
    logger.warning("Concurrent modification detected while updating collection with ID: " + id);
    throw new ConcurrentModificationException("The collection has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public void recoverFromResourceNotFound(
          ResourceNotFoundException ex,
          @NonNull Long collectionId,
          @NonNull Long bookId) {
    logger.warning("Collection not found while processing book ID: " + bookId + " for collection ID: " + collectionId);
    throw ex; // Re-throw the original exception
  }


}