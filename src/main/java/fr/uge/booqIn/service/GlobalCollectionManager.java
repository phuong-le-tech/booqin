package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.*;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.BookCollectionFactory;
import fr.uge.booqIn.factory.CriteriaStrategyFactory;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Gestionnaire responsable de l'initialisation et de la maintenance des collections globales du système.
 * Utilise BookCollectionFactory pour la création des collections.
 */
@Component
public class GlobalCollectionManager {
    private static final Logger logger = Logger.getLogger(GlobalCollectionManager.class.getName());

    private final BookCollectionRepository collectionRepository;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CriteriaStrategyFactory criteriaFactory;

    @Autowired
    public GlobalCollectionManager(
            BookCollectionRepository collectionRepository,
            AuthorRepository authorRepository,
            BookRepository bookRepository,
            UserRepository userRepository,
            CriteriaStrategyFactory criteriaFactory) {
        this.collectionRepository = collectionRepository;
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.criteriaFactory = criteriaFactory;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeGlobalCollections() {
        logger.info("Initializing global collections...");
        User adminUser = getOrCreateAdminUser();
        createRecentBooksCollection(adminUser);
        createAuthorCollections(adminUser);
        logger.info("Global collections initialized successfully.");
    }

    private void createRecentBooksCollection(User owner) {
        String collectionTitle = "Recent Books";
        Optional<AbstractBookCollection> existingCollection =
                collectionRepository.findByTitle(collectionTitle);

        if (existingCollection.isPresent()) {
            logger.info("Recent books collection already exists. Skipping creation.");

            if (existingCollection.get() instanceof GlobalBookCollection globalCollection) {
                updateRecentBooksCollection(globalCollection);
            } else if (existingCollection.get() instanceof SmartBookCollection smartCollection) {
                // For backward compatibility
                List<Book> allBooks = bookRepository.findAll();
                smartCollection.updateBooks(allBooks);
                collectionRepository.save(smartCollection);
            }
            return;
        }

        // Create a global collection for recent books using criteria
        GlobalBookCollection recentBooks = BookCollectionFactory.createGlobalCollection(
                collectionTitle,
                "Books added in the last 30 days",
                owner,
                "recent:30",
                bookRepository.findAll()
        );

        // Save the collection
        collectionRepository.save(recentBooks);
        logger.info("Recent books collection created with " + recentBooks.getBooks().size() + " books.");
    }

    /**
     * Updates the recent books collection with the latest books.
     */
    @Transactional
    public void updateRecentBooksCollection(GlobalBookCollection recentBooksCollection) {
        if (recentBooksCollection.getCriteriaStrategy() == null) {
            recentBooksCollection.initializeCriteriaStrategy(criteriaFactory);
        }

        List<Book> allBooks = bookRepository.findAll();
        recentBooksCollection.updateBooks(allBooks);
        collectionRepository.save(recentBooksCollection);
        logger.info("Recent books collection updated with " + recentBooksCollection.getBooks().size() + " books.");
    }

    /**
     * Creates a global collection for each author in the system.
     */
    private void createAuthorCollections(User owner) {
        // Get all authors
        List<Author> authors = authorRepository.findAll();
        logger.info("Creating collections for " + authors.size() + " authors...");

        for (Author author : authors) {
            String collectionTitle = "Books by " + author.getFullName();

            // Check if the collection already exists
            if (collectionRepository.findByTitle(collectionTitle).isPresent()) {
                continue;
            }

            // Create author collection with criteria
            String criteriaString = "author:" + author.getFullName();
            GlobalBookCollection authorCollection = BookCollectionFactory.createGlobalCollection(
                    collectionTitle,
                    "Collection of all books by " + author.getFullName(),
                    owner,
                    criteriaString,
                    bookRepository.findAll()
            );

            // Save the collection
            collectionRepository.save(authorCollection);
            logger.info("Created collection for author: " + author.getFullName() + " with " + authorCollection.getBooks().size() + " books.");
        }
    }

    /**
     * Updates author collections when a new book is added.
     * This method is called by the event listener when a book is added or updated.
     */
    @Transactional
    public void updateAuthorCollectionsForNewBook(Book book) {
        logger.info("Updating author collections for book: " + book.getTitle());

        if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
            logger.info("Book has no authors, skipping collection update");
            return;
        }

        for (Author author : book.getAuthors()) {
            String collectionTitle = "Books by " + author.getFullName();

            Optional<AbstractBookCollection> collection = collectionRepository.findByTitle(collectionTitle);

            if (collection.isPresent()) {
                AbstractBookCollection authorCollection = collection.get();

                // If it's a GlobalBookCollection with criteria, update it
                if (authorCollection instanceof GlobalBookCollection globalCollection) {
                    if (globalCollection.getCriteriaStrategy() == null) {
                        globalCollection.initializeCriteriaStrategy(criteriaFactory);
                    }

                    // Just add the book if it matches criteria
                    if (globalCollection.getCriteriaStrategy().satisfy(book)) {
                        if (!authorCollection.getBooks().contains(book)) {
                            authorCollection.addBook(book);
                            collectionRepository.save(authorCollection);
                            logger.info("Added new book to author collection: " + collectionTitle);
                        }
                    }
                }
            }
            else {
                // Create new author collection with criteria
                User adminUser = getOrCreateAdminUser();
                String criteriaString = "author:" + author.getFullName();

                GlobalBookCollection newCollection = BookCollectionFactory.createGlobalCollection(
                        collectionTitle,
                        "Collection of all books by " + author.getFullName(),
                        adminUser,
                        criteriaString,
                        List.of(book)
                );

                collectionRepository.save(newCollection);
                logger.info("Created new author collection for: " + author.getFullName());
            }
        }
    }

    @Transactional
    public void refreshRecentBooksCollection() {
        logger.info("Scheduled refresh of recent books collection...");

        Optional<AbstractBookCollection> collection =
                collectionRepository.findByTitle("Recent Books");

        if (collection.isPresent()) {
            if (collection.get() instanceof GlobalBookCollection globalCollection) {
                updateRecentBooksCollection(globalCollection);
            }
            // For backward compatibility
            else if (collection.get() instanceof SmartBookCollection smartCollection) {
                List<Book> allBooks = bookRepository.findAll();
                smartCollection.updateBooks(allBooks);
                collectionRepository.save(smartCollection);
                logger.info("Recent books collection (SmartBookCollection) updated with " + smartCollection.getBooks().size() + " books.");
            }
        }
    }

    private User getOrCreateAdminUser() {
        Optional<User> adminUser = userRepository.findByRole(UserRole.ADMIN).stream().findFirst();
        if (adminUser.isEmpty()) {
            adminUser = userRepository.findAll().stream().findFirst();

            if (adminUser.isEmpty()) {
                throw new IllegalStateException("No users found in the system. Cannot create global collections.");
            }
        }
        return adminUser.get();
    }
}