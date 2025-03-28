package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.*;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.factory.BookCollectionFactory;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class GlobalCollectionManager {
  private static final Logger logger = Logger.getLogger(GlobalCollectionManager.class.getName());
  private static final int RECENT_DAYS_RANGE = 30;

  private final BookCollectionRepository collectionRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;

  public GlobalCollectionManager(
          BookCollectionRepository collectionRepository,
          BookRepository bookRepository,
          UserRepository userRepository) {
    this.collectionRepository = Objects.requireNonNull(collectionRepository, "collectionRepository cannot be null");
    this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository cannot be null");
    this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
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
              collectionRepository.findFirstRecentBookCollection();

      if (existingCollection.isPresent()) {
        logger.info("Recent books collection already exists. Updating...");

        if (existingCollection.get() instanceof RecentBookCollection recentCollection) {
          updateRecentBooksCollection(recentCollection);
        } else {
          logger.warning("Found collection with title 'Recent Books' but it's not a RecentBookCollection");
          User collectionOwner = existingCollection.get().getOwner();
          collectionRepository.delete(existingCollection.get());
          createNewRecentBooksCollection(collectionOwner);
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

    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RECENT_DAYS_RANGE);
    List<Book> recentBooks = bookRepository.findByCreatedAtAfter(cutoffDate);

    RecentBookCollection recentBooksCollection = BookCollectionFactory.createRecentBookCollection(owner);

    recentBooks.forEach(recentBooksCollection::addBook);

    collectionRepository.save(recentBooksCollection);
    logger.info("Recent books collection created with " + recentBooksCollection.getBooks().size() + " books.");
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
  public void updateRecentBooksCollection(RecentBookCollection recentBooksCollection) {
    Objects.requireNonNull(recentBooksCollection, "Collection cannot be null");

    logger.info("Updating recent books collection with ID: " + recentBooksCollection.getId());

    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(recentBooksCollection.getDaysRange());
    List<Book> recentBooks = bookRepository.findByCreatedAtAfter(cutoffDate);

    recentBooksCollection.getBooks().clear();
    recentBooks.forEach(recentBooksCollection::addBook);
    recentBooksCollection.setUpdatedAt(LocalDateTime.now());

    collectionRepository.save(recentBooksCollection);
    logger.info("Recent books collection updated with " + recentBooksCollection.getBooks().size() + " books.");
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
      Optional<AbstractBookCollection> collectionOpt = collectionRepository.findByTitle(collectionTitle);

      if (collectionOpt.isPresent()) {
        AbstractBookCollection collection = collectionOpt.get();

        if (collection instanceof AuthorBookCollection authorCollection) {
          if (!authorCollection.getBooks().contains(book)) {
            authorCollection.addBook(book);
            collectionRepository.save(authorCollection);
            logger.info("Added new book to author collection: " + authorCollection.getTitle());
          } else {
            logger.fine("Book already in author collection, skipping");
          }
        } else {
          logger.warning("Collection found for author " + author.getFullName() +
                  " but it's not an AuthorBookCollection");

          User collectionOwner = collection.getOwner();
          collectionRepository.delete(collection);
          createNewAuthorCollection(collectionOwner, author);
        }
      } else {
        createNewAuthorCollection(adminUser, author);
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, "Error processing author collection for " + author.getFullName(), e);
    }
  }

  private void createNewAuthorCollection(User adminUser, Author author) {
    List<Book> authorBooks = findBooksByAuthor(author);

    AuthorBookCollection newCollection = BookCollectionFactory.createAuthorBookCollection(adminUser, author.getFullName());

    authorBooks.forEach(newCollection::addBook);

    collectionRepository.save(newCollection);
    logger.info("Created new author collection for: " + author.getFullName() +
            " with ID: " + newCollection.getId());
  }

  private List<Book> findBooksByAuthor(Author author) {
    String firstName = author.getFirstName();
    String lastName = author.getLastName();

    if (firstName != null && lastName != null) {
      return bookRepository.findByAuthor(firstName, lastName);
    } else {
      return bookRepository.findAll().stream()
              .filter(book -> book.getAuthors().stream()
                      .anyMatch(a -> Objects.equals(a.getFullName(), author.getFullName())))
              .collect(Collectors.toList());
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

      Optional<AbstractBookCollection> collectionOpt =
              collectionRepository.findFirstRecentBookCollection();

      if (collectionOpt.isPresent()) {
        AbstractBookCollection collection = collectionOpt.get();
        if (collection instanceof RecentBookCollection recentCollection) {
          updateRecentBooksCollection(recentCollection);
        } else {
          logger.warning("Found collection with title 'Recent Books' but it's not a RecentBookCollection");
          User owner = collection.getOwner();
          collectionRepository.delete(collection);
          createNewRecentBooksCollection(owner);
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
  public void recoverFromOptimisticLockingFailureUpdate(ObjectOptimisticLockingFailureException ex, RecentBookCollection collection) {
    logRecoveryFailure("update recent books collection", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureUpdateAuthor(ObjectOptimisticLockingFailureException ex, Book book) {
    logRecoveryFailure("update author collections for book: " + book.getTitle(), ex);
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
}