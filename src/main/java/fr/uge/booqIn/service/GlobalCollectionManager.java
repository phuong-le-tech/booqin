package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.*;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.factory.BookCollectionFactory;
import fr.uge.booqIn.factory.CriteriaStrategyFactory;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class GlobalCollectionManager {
  private static final Logger logger = Logger.getLogger(GlobalCollectionManager.class.getName());
  private static final String RECENT_BOOKS_COLLECTION_TITLE = "Recent Books";
  private static final String RECENT_BOOKS_COLLECTION_DESC = "Books added in the last 30 days";
  private static final String RECENT_BOOKS_CRITERIA = "recent:30";

  private final BookCollectionRepository collectionRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final CriteriaStrategyFactory criteriaFactory;

  public GlobalCollectionManager(
          BookCollectionRepository collectionRepository,
          BookRepository bookRepository,
          UserRepository userRepository,
          CriteriaStrategyFactory criteriaFactory) {
    this.collectionRepository = Objects.requireNonNull(collectionRepository, "collectionRepository cannot be null");
    this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository cannot be null");
    this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
    this.criteriaFactory = Objects.requireNonNull(criteriaFactory, "criteriaFactory cannot be null");
  }

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts:3}",
          retryFor = {
                  ObjectOptimisticLockingFailureException.class,
                  QueryTimeoutException.class,
                  TransactionException.class
          },
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay:1000}")
  )
  public void initializeGlobalCollections() {
    logger.info("Initializing global collections...");
    try {
      User adminUser = getOrCreateAdminUser();
      createOrUpdateRecentBooksCollection(adminUser);
      logger.info("Global collections initialized successfully.");
    } catch (IllegalStateException e) {
      logger.log(Level.SEVERE, "Failed to initialize global collections: no admin user available", e);
      throw e;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unexpected error during global collections initialization", e);
      throw e;
    }
  }

  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts:3}",
          retryFor = {
                  ObjectOptimisticLockingFailureException.class,
                  QueryTimeoutException.class
          },
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay:1000}")
  )
  public void createOrUpdateRecentBooksCollection(User owner) {
    Objects.requireNonNull(owner, "Owner cannot be null");
    logger.info("Creating or updating recent books collection");
    try {
      Optional<AbstractBookCollection> existingCollection =
              collectionRepository.findByTitle(RECENT_BOOKS_COLLECTION_TITLE);

      if (existingCollection.isPresent()) {
        logger.info("Recent books collection already exists. Updating...");

        if (existingCollection.get() instanceof GlobalBookCollection globalCollection) {
          updateRecentBooksCollection(globalCollection);
        } else {
          logger.warning("Found collection with title 'Recent Books' but it's not a GlobalBookCollection");
        }
      } else {
        createNewRecentBooksCollection(owner);
      }
    } catch (DataAccessException e) {
      logger.log(Level.SEVERE, "Database error while creating/updating recent books collection", e);
      throw e;
    }
  }

  private void createNewRecentBooksCollection(User owner) {
    logger.info("Creating new recent books collection");

    List<Book> books = bookRepository.findAll();

    GlobalBookCollection recentBooks = BookCollectionFactory.createGlobalCollection(
            RECENT_BOOKS_COLLECTION_TITLE,
            RECENT_BOOKS_COLLECTION_DESC,
            owner,
            RECENT_BOOKS_CRITERIA,
            books
    );

    collectionRepository.save(recentBooks);
    logger.info("Recent books collection created with " + recentBooks.getBooks().size() + " books.");
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts:3}",
          retryFor = {
                  ObjectOptimisticLockingFailureException.class,
                  QueryTimeoutException.class
          },
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay:1000}")
  )
  public void updateRecentBooksCollection(GlobalBookCollection recentBooksCollection) {
    Objects.requireNonNull(recentBooksCollection, "Collection cannot be null");

    logger.info("Updating recent books collection with ID: " + recentBooksCollection.getId());

    if (recentBooksCollection.getCriteriaStrategy() == null) {
      recentBooksCollection.initializeCriteriaStrategy(criteriaFactory);
    }

    // Process books in batches to improve performance and reduce memory usage
    processBooksInBatches(recentBooksCollection);

    collectionRepository.save(recentBooksCollection);
    logger.info("Recent books collection updated with " + recentBooksCollection.getBooks().size() + " books.");

  }

  private void processBooksInBatches(GlobalBookCollection collection) {
      List<Book> bookBatch = bookRepository.findAll();
      collection.updateBooks(bookBatch.stream().toList());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts:3}",
          retryFor = {
                  ObjectOptimisticLockingFailureException.class,
                  QueryTimeoutException.class
          },
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay:1000}")
  )
  public void updateAuthorCollectionsForNewBook(Book book) {
    Objects.requireNonNull(book, "Book cannot be null");
    logger.info("Updating author collections for book: " + book.getTitle());

    if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
      logger.info("Book has no authors, skipping collection update");
      return;
    }

    try {
      User adminUser = getOrCreateAdminUser();

      for (Author author : book.getAuthors()) {
        if (author == null || author.getFullName() == null) {
          logger.warning("Skipping null author or author with null name");
          continue;
        }

        updateSingleAuthorCollection(book, adminUser, author);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error updating author collections for book: " + book.getTitle(), e);
      throw e;
    }
  }

  private void updateSingleAuthorCollection(Book book, User adminUser, Author author) {
    String collectionTitle = "Books by " + author.getFullName();
    try {
      Optional<AbstractBookCollection> collection = collectionRepository.findByTitle(collectionTitle);

      if (collection.isPresent()) {
        AbstractBookCollection authorCollection = collection.get();

        if (authorCollection instanceof GlobalBookCollection globalCollection) {
          updateExistingAuthorCollection(book, globalCollection);
        } else {
          logger.warning("Collection found for author " + author.getFullName() +
                  " but it's not a GlobalBookCollection");
        }
      } else {
        createNewAuthorCollection(book, adminUser, author, collectionTitle);
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, "Error processing author collection for " + author.getFullName(), e);
      // Continue with other authors instead of letting the exception propagate
    }
  }

  private void updateExistingAuthorCollection(Book book, GlobalBookCollection globalCollection) {
    if (globalCollection.getCriteriaStrategy() == null) {
      globalCollection.initializeCriteriaStrategy(criteriaFactory);
    }

    if (globalCollection.getCriteriaStrategy().satisfy(book)) {
      if (!globalCollection.getBooks().contains(book)) {
        globalCollection.addBook(book);
        collectionRepository.saveAndFlush(globalCollection);
        logger.info("Added new book to author collection: " + globalCollection.getTitle());
      } else {
        logger.fine("Book already in author collection, skipping");
      }
    }
  }

  private void createNewAuthorCollection(Book book, User adminUser, Author author, String collectionTitle) {
    String criteriaString = "author:" + author.getFullName();
    GlobalBookCollection newCollection = createAndSaveAuthorCollection(
            collectionTitle,
            "Collection of all books by " + author.getFullName(),
            adminUser,
            criteriaString,
            List.of(book)
    );
    logger.info("Created new author collection for: " + author.getFullName() +
            " with ID: " + newCollection.getId());
  }

  private GlobalBookCollection createAndSaveAuthorCollection(
          String title, String description, User owner, String criteria, List<Book> books) {

    Objects.requireNonNull(title, "Title cannot be null");
    Objects.requireNonNull(owner, "Owner cannot be null");
    Objects.requireNonNull(criteria, "Criteria cannot be null");
    Objects.requireNonNull(books, "Books list cannot be null");

    logger.info("Creating new collection: " + title + " with criteria: " + criteria);

    try {
      GlobalBookCollection newCollection = BookCollectionFactory.createGlobalCollection(
              title, description, owner, criteria, books
      );
      GlobalBookCollection savedCollection = collectionRepository.saveAndFlush(newCollection);
      logger.info("Collection saved successfully with ID: " + savedCollection.getId());
      return savedCollection;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error creating author collection: " + title, e);
      throw e;
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts:3}",
          retryFor = {
                  ObjectOptimisticLockingFailureException.class,
                  QueryTimeoutException.class
          },
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay:1000}")
  )
  public void refreshRecentBooksCollection() {
    try {
      logger.info("Scheduled refresh of recent books collection...");

      Optional<AbstractBookCollection> collection =
              collectionRepository.findByTitle(RECENT_BOOKS_COLLECTION_TITLE);

      if (collection.isPresent()) {
        if (collection.get() instanceof GlobalBookCollection globalCollection) {
          updateRecentBooksCollection(globalCollection);
        } else {
          logger.warning("Found collection with title 'Recent Books' but it's not a GlobalBookCollection");
        }
      } else {
        logger.warning("Recent books collection not found. Creating it...");
        createOrUpdateRecentBooksCollection(getOrCreateAdminUser());
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error refreshing recent books collection", e);
      throw e;
    }
  }

  private User getOrCreateAdminUser() {
    Optional<User> adminUser = userRepository.findByRole(UserRole.ADMIN).stream().findFirst();
    if (adminUser.isEmpty()) {
      logger.info("No admin user found. Using first available user.");
      adminUser = userRepository.findAll().stream().findFirst();

      if (adminUser.isEmpty()) {
        String errorMsg = "No users found in the system. Cannot create global collections.";
        logger.severe(errorMsg);
        throw new IllegalStateException(errorMsg);
      }
    }
    return adminUser.get();
  }

  /* Recovery methods for retryable operations */

  @Recover
  public void recoverFromOptimisticLockingFailureInit(ObjectOptimisticLockingFailureException ex) {
    logRecoveryFailure("initialize global collections", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureCreateUpdate(ObjectOptimisticLockingFailureException ex, User owner) {
    logRecoveryFailure("create/update recent books collection", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureUpdate(ObjectOptimisticLockingFailureException ex, GlobalBookCollection collection) {
    logRecoveryFailure("update recent books collection", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureUpdateAuthor(ObjectOptimisticLockingFailureException ex, Book book) {
    logRecoveryFailure("update author collections for book: " + book.getTitle(), ex);
  }


  @Recover
  public GlobalBookCollection recoverFromOptimisticLockingFailureCreateAuthor(
          ObjectOptimisticLockingFailureException ex, String title, String description,
          User owner, String criteria, List<Book> books) {

    logRecoveryFailure("create author collection: " + title, ex);

    throw new ConcurrentModificationException("Failed to create author collection due to concurrent modifications", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureRefresh(ObjectOptimisticLockingFailureException ex) {
    logRecoveryFailure("refresh recent books collection", ex);
  }

  @Recover
  public void recoverFromQueryTimeout(QueryTimeoutException ex) {
    logger.log(Level.SEVERE, "Database query timeout occurred", ex);
    throw new RuntimeException("Database operation timed out after multiple retries", ex);
  }

  @Recover
  public void recoverFromTransactionFailure(TransactionException ex) {
    logger.log(Level.SEVERE, "Transaction failure occurred", ex);
    throw new RuntimeException("Transaction failed after multiple retries", ex);
  }

  private void logRecoveryFailure(String operation, Exception ex) {
    logger.log(Level.SEVERE, "Failed to " + operation + " after multiple retries", ex);
    throw new ConcurrentModificationException("Failed to " + operation + " due to concurrent modifications", ex);
  }

  @Transactional(readOnly = true)
  public GlobalBookCollection findGlobalCollectionByTitle(String title) {
    Objects.requireNonNull(title, "Title cannot be null");

    AbstractBookCollection collection = collectionRepository.findByTitle(title)
            .orElseThrow(() -> new ResourceNotFoundException("Collection not found with title: " + title));

    if (!(collection instanceof GlobalBookCollection)) {
      throw new IllegalArgumentException("Collection with title '" + title + "' is not a GlobalBookCollection");
    }

    return (GlobalBookCollection) collection;
  }

  @Transactional(readOnly = true)
  public List<GlobalBookCollection> getAllGlobalCollections() {
    return collectionRepository.findAll().stream()
            .filter(c -> c instanceof GlobalBookCollection)
            .map(c -> (GlobalBookCollection) c)
            .collect(Collectors.toList());
  }
}