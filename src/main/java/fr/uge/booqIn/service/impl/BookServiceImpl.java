package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.event.BooqInEventPublisher;
import fr.uge.booqIn.event.publishable.BookAddedEvent;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.service.ImageService;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.specification.BookSpecification;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@Service
public class BookServiceImpl implements BookServiceInterface {
  private static final Logger logger = Logger.getLogger(BookServiceImpl.class.getName());
  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;
  private final BookMapper bookMapper;
  private final BooqInEventPublisher eventPublisher;
  private final ImageService imageService;
  @Value("${file.upload-dir}")
  private String uploadDir;

  public BookServiceImpl(
          BookRepository bookRepository,
          AuthorRepository authorRepository,
          BookMapper bookMapper,
          BooqInEventPublisher eventPublisher,
          ImageService imageService) {
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
    this.bookMapper = bookMapper;
    this.eventPublisher = eventPublisher;
    this.imageService = imageService;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<BookIndex> findBooks(String title, String author, String isbn, @NonNull Pageable pageable) {
    // Build the specification for filtering
    Specification<Book> spec = Specification.where(null);

    if (title != null && !title.isEmpty()) {
      spec = spec.and(BookSpecification.hasTitle(title));
    }

    if (author != null && !author.isEmpty()) {
      spec = spec.and(BookSpecification.hasAuthor(author));
    }

    if (isbn != null && !isbn.isEmpty()) {
      spec = spec.and(BookSpecification.hasIsbn(isbn));
    }

    return bookRepository.findAll(spec, pageable)
            .map(bookMapper::toBookIndex);
  }

  @Override
  @Transactional(readOnly = true)
  public BookShow findById(@NonNull Long id) {
    Book book = findBookById(id);
    return bookMapper.toBookShow(book);
  }

  @Override
  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {
          ObjectOptimisticLockingFailureException.class,
          ConcurrentModificationException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public Book createBook(@NonNull @Valid Book book) {
    // Save authors first if they don't have IDs
    if (book.getAuthors() != null) {
      book.getAuthors().stream()
              .filter(author -> author.getId() == null)
              .forEach(authorRepository::save);
    }

    // Check for duplicate ISBNs
    boolean isbnExists = book.getIsbns().stream()
            .anyMatch(isbn -> findByIsbn(isbn).isPresent());
    if (isbnExists) {
      throw new DuplicateResourceException("Book", "ISBN", book.getIsbns().toString());
    }

    book.setCreatedAt(LocalDateTime.now());
    book.setUpdatedAt(LocalDateTime.now());
    Book savedBook = bookRepository.save(book);
    savedBook = downloadAndSaveCoverImage(savedBook);
    return savedBook;
  }

  @Override
  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {
          ObjectOptimisticLockingFailureException.class,
          ConcurrentModificationException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public Book updateBook(@NonNull @Valid Book book) {
    // Check if book exists
    if (!bookRepository.existsById(book.getId())) {
      throw new ResourceNotFoundException("Book", "id", book.getId());
    }

    if (book.getAuthors() != null) {
      processAuthors(book.getAuthors());
    }

    // Check for duplicate ISBNs in other books
    for (String isbn : book.getIsbns()) {
      Optional<Book> existingBook = findByIsbn(isbn);
      if (existingBook.isPresent() && !existingBook.get().getId().equals(book.getId())) {
        throw new DuplicateResourceException("Book", "ISBN", isbn);
      }
    }

    book.setUpdatedAt(LocalDateTime.now());
    return bookRepository.save(book);
  }

  @Override
  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {
          ObjectOptimisticLockingFailureException.class,
          ConcurrentModificationException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public BookShow patchBook(@NonNull Long id, @NonNull @Valid BookUpdate bookUpdate) {
    Book book = findBookById(id);

    // Apply updates if present
    bookUpdate.title().ifPresent(book::setTitle);
    bookUpdate.description().ifPresent(book::setDescription);
    bookUpdate.publicationDate().ifPresent(book::setPublicationDate);

    // Handle ISBNs update (check for duplicates)
    bookUpdate.isbns().ifPresent(isbns -> {
      // Check for duplicate ISBNs
      for (String isbn : isbns) {
        Optional<Book> existingBook = findByIsbn(isbn);
        if (existingBook.isPresent() && !existingBook.get().getId().equals(id)) {
          throw new DuplicateResourceException("Book", "ISBN", isbn);
        }
      }
      book.getIsbns().clear();
      book.setIsbns(isbns);
    });

    book.setUpdatedAt(LocalDateTime.now());
    Book savedBook = bookRepository.save(book);
    return bookMapper.toBookShow(savedBook);
  }

  @Override
  @Transactional
  public void deleteBook(@NonNull Long id) {
    Book book = findBookById(id);
    bookRepository.delete(book);
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {
          ObjectOptimisticLockingFailureException.class,
          ConcurrentModificationException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public void saveFromApi(@NonNull Book toSave) {
    if (toSave.getAuthors() != null) {
      processAuthors(toSave.getAuthors());
    }
    // Check if a book with one of the ISBNs already exists
    Optional<Book> existingBookOpt = findExistingBookByIsbns(toSave.getIsbns());

    var savedBook = existingBookOpt.map(book -> updateExistingBook(book, toSave))
            .orElseGet(() -> createNewBook(toSave));

    downloadAndSaveCoverImage(savedBook);
    eventPublisher.publishEvent(new BookAddedEvent(savedBook.getId(), existingBookOpt.isEmpty()));
  }

  @Override
  @Transactional(readOnly = true)
  public Book findBookById(@NonNull Long id) {
    Book bookWithAuthors = bookRepository.findByIdWithAuthors(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

    Book bookWithIsbns = bookRepository.findByIdWithIsbns(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

    Book bookWithCategories = bookRepository.findByIdWithCategories(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

    bookWithAuthors.setIsbns(bookWithIsbns.getIsbns());
    bookWithAuthors.setCategories(bookWithCategories.getCategories());

    return bookWithAuthors;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Book> findAll() {
    return bookRepository.findAll();
  }

  private Optional<Book> findExistingBookByIsbns(Set<String> isbns) {
    if (isbns == null || isbns.isEmpty()) {
      return Optional.empty();
    }

    return isbns.stream()
            .map(this::findByIsbn)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
  }

  private Book updateExistingBook(Book existingBook, Book newBook) {
    // Update description if needed
    if (isEmptyOrNull(existingBook.getDescription()) && !isEmptyOrNull(newBook.getDescription())) {
      existingBook.setDescription(newBook.getDescription());
    }

    // Add new ISBNs
    addNewIsbns(existingBook, newBook.getIsbns());

    // Add new categories
    addNewCategories(existingBook, newBook.getCategories());

    // Add new authors
    if (newBook.getAuthors() != null && !newBook.getAuthors().isEmpty()) {
      processAuthors(newBook.getAuthors());
      if (existingBook.getAuthors() == null) {
        existingBook.setAuthors(new HashSet<>());
      }
      existingBook.getAuthors().addAll(newBook.getAuthors());
    }

    // Update image url
    if (isEmptyOrNull(existingBook.getCoverImageUrl()) && !isEmptyOrNull(newBook.getCoverImageUrl())) {
      existingBook.setCoverImageUrl(newBook.getCoverImageUrl());
    }

    // Update modification date
    existingBook.setUpdatedAt(LocalDateTime.now());

    return bookRepository.save(existingBook);
  }

  private void addNewIsbns(Book existingBook, Set<String> newIsbns) {
    if (newIsbns == null)
      return;

    Set<String> existingIsbns = new HashSet<>(existingBook.getIsbns());
    for (String isbn : newIsbns) {
      if (!existingIsbns.contains(isbn) && findByIsbn(isbn).isEmpty()) {
        existingBook.addIsbn(isbn);
      }
    }
  }

  private void addNewCategories(Book existingBook, Set<String> newCategories) {
    if (newCategories == null)
      return;

    newCategories.forEach(existingBook::addCategory);
  }


  private Book createNewBook(Book toSave) {
    // Check that none of the ISBNs exists in any book
    for (String isbn : toSave.getIsbns()) {
      if (findByIsbn(isbn).isPresent()) {
        throw new DuplicateResourceException("Book", "ISBN", isbn);
      }
    }

    if (toSave.getAuthors() == null) {
      toSave.setAuthors(new HashSet<>());
    } else {
      processAuthors(toSave.getAuthors());
    }

    toSave.getAuthors().stream()
            .filter(author -> author.getId() == null)
            .forEach(authorRepository::save);

    toSave.setCreatedAt(LocalDateTime.now());
    toSave.setUpdatedAt(LocalDateTime.now());

    return bookRepository.save(toSave);
  }

  private boolean isEmptyOrNull(String str) {
    return str == null || str.isEmpty();
  }


  private Optional<Book> findByIsbn(String isbn) {
    return bookRepository.findBookByIsbnInSet(isbn);
  }

  private Book downloadAndSaveCoverImage(Book book) {
    if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()
            && (book.getCoverImagePath() == null || book.getCoverImagePath().isEmpty())) {
      try {
        var imagePath = imageService.downloadBookCoverFromUrl(book.getCoverImageUrl(), book.getId());
        book.setCoverImagePath(imagePath);

        try {
          var originalPath = Paths.get(uploadDir, imagePath);
          var thumbnailPath = imageService.createThumbnail(originalPath, book.getId());
          book.setThumbnailPath(thumbnailPath);
        } catch (Exception e) {
          logger.warning("Échec de l'assignation du chemin de miniature: " + e.getMessage());
        }
        book = bookRepository.save(book);
      } catch (Exception e) {
        logger.warning("Échec du téléchargement de l'image de couverture: " + e.getMessage());
      }
    }
    return book;
  }

  private void processAuthors(Set<Author> authors) {
    if (authors == null || authors.isEmpty()) {
      return;
    }

    Set<Author> processedAuthors = new HashSet<>();

    for (Author author : authors) {
      if (author.getId() != null) {
        processedAuthors.add(author);
        continue;
      }

      Optional<Author> existingAuthor = authorRepository.findByFirstNameAndLastName(
              author.getFirstName(), author.getLastName());

      if (existingAuthor.isPresent()) {
        processedAuthors.add(existingAuthor.get());
      } else {
        Author savedAuthor = authorRepository.save(author);
        processedAuthors.add(savedAuthor);
      }
    }

    authors.clear();
    authors.addAll(processedAuthors);
  }

  // Recovery methods for retryable operations

  @Recover
  public Book recoverFromOptimisticLockingFailureCreate(
          ObjectOptimisticLockingFailureException ex, @NonNull @Valid Book book) {
    throw new ConcurrentModificationException("The book has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public Book recoverFromOptimisticLockingFailureUpdate(
          ObjectOptimisticLockingFailureException ex, @NonNull @Valid Book book) {
    throw new ConcurrentModificationException("The book has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public BookShow recoverFromOptimisticLockingFailurePatch(
          ObjectOptimisticLockingFailureException ex, @NonNull Long id, @NonNull @Valid BookUpdate bookUpdate) {
    throw new ConcurrentModificationException("The book has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureSaveFromApi(
          ObjectOptimisticLockingFailureException ex, Book toSave) {
    throw new ConcurrentModificationException("The book has been modified by someone else. Please try again.", ex);
  }
}