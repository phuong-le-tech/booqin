package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.specification.BookSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.logging.Logger;

@Service
public class BookServiceImpl implements BookServiceInterface {
  private static final Logger logger = Logger.getLogger(BookServiceImpl.class.getName());
  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;
  private final BookMapper bookMapper;

  public BookServiceImpl(BookRepository bookRepository, AuthorRepository authorRepository, BookMapper bookMapper) {
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
    this.bookMapper = bookMapper;
  }

  @Override
  @Transactional
  public Page<BookIndex> findBooks(String title, String author, String isbn, Pageable pageable) {
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
  @Transactional
  public BookShow findById(@NonNull Long id) {
    Book book = findBookById(id);
    return bookMapper.toBookShow(book);
  }

  public Book createBook(@NonNull @Validated Book book) {
    if (book.getAuthors() != null) {
      book.getAuthors().stream()
              .filter(author -> author.getId() == null)
              .forEach(authorRepository::save);
    }

    boolean isbnExists = book.getIsbns().stream()
            .anyMatch(isbn -> findByIsbn(isbn).isPresent());
    if (isbnExists) {
      throw new DuplicateResourceException("Book", "ISBN", book.getIsbns().toString());
    }

    return bookRepository.save(book);
  }

  @Override
  @Transactional
  public Book updateBook(@NonNull @Validated Book book) {
    if (!bookRepository.existsById(book.getId())) {
      throw new ResourceNotFoundException("Book", "id", book.getId());
    }

    book.setUpdatedAt(new Date());
    return bookRepository.save(book);
  }

  @Override
  @Transactional
  public BookShow patchBook(@NonNull Long id, @NonNull @Validated BookUpdate bookUpdate) {
    Book book = findBookById(id);
    System.out.println(book);

    bookUpdate.title().ifPresent(book::setTitle);
    bookUpdate.description().ifPresent(book::setDescription);
    bookUpdate.publicationDate().ifPresent(book::setPublicationDate);

    bookUpdate.isbns().ifPresent(isbns -> {
      book.getIsbns().clear();
      book.setIsbns(isbns);
    });
    book.setUpdatedAt(new Date());
    return bookMapper.toBookShow(bookRepository.save(book));
  }

  @Override
  @Transactional
  public void deleteBook(@NonNull Long id) {
    Book book = findBookById(id);
    bookRepository.delete(book);
  }

  @Transactional
  public Book saveFromApi(Book toSave) {
    Objects.requireNonNull(toSave, "Book to save cannot be null");

    // Check if a book with one of the ISBNs already exists
    Optional<Book> existingBookOpt = findExistingBookByIsbns(toSave.getIsbns());

      return existingBookOpt.map(book -> updateExistingBook(book, toSave)).orElseGet(() -> createNewBook(toSave));
  }

  private Optional<Book> findExistingBookByIsbns(Set<String> isbns) {
    return isbns.stream()
            .map(this::findByIsbn)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
  }

  private Book updateExistingBook(Book existingBook, Book newBook) {
    logger.info("A book with one of the ISBNs already exists, updating information");

    // Update description if needed
    if (isEmptyOrNull(existingBook.getDescription()) && !isEmptyOrNull(newBook.getDescription())) {
      existingBook.setDescription(newBook.getDescription());
    }

    // Add new ISBNs
    addNewIsbns(existingBook, newBook.getIsbns());

    // Add new categories
    addNewCategories(existingBook, newBook.getCategories());

    // Add new authors
    addNewAuthors(existingBook, newBook.getAuthors());

    // Update modification date
    existingBook.setUpdatedAt(new Date());

    return bookRepository.save(existingBook);
  }

  private void addNewIsbns(Book existingBook, Set<String> newIsbns) {
    if (newIsbns == null) return;

    Set<String> existingIsbns = new HashSet<>(existingBook.getIsbns());
    for (String isbn : newIsbns) {
      if (!existingIsbns.contains(isbn) && findByIsbn(isbn).isEmpty()) {
        existingBook.addIsbn(isbn);
      }
    }
  }

  private void addNewCategories(Book existingBook, Set<String> newCategories) {
    if (newCategories == null) return;

    newCategories.forEach(existingBook::addCategory);
  }

  private void addNewAuthors(Book existingBook, Set<Author> newAuthors) {
    if (newAuthors == null) return;

    // Save new authors that don't have an ID
    newAuthors.stream()
            .filter(author -> author.getId() == null)
            .forEach(authorRepository::save);

    // Add authors to the existing book
    if (existingBook.getAuthors() == null) {
      existingBook.setAuthors(new HashSet<>());
    }
    existingBook.getAuthors().addAll(newAuthors);
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
    }

    toSave.getAuthors().stream()
            .filter(author -> author.getId() == null)
            .forEach(authorRepository::save);

    return bookRepository.save(toSave);
  }

  private boolean isEmptyOrNull(String str) {
    return str == null || str.isEmpty();
  }

  private Book findBookById(Long id) {
    Book bookWithAuthors = bookRepository.findByIdWithAuthors(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

    Book bookWithIsbns = bookRepository.findByIdWithIsbns(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

    bookWithAuthors.setIsbns(bookWithIsbns.getIsbns());

    return bookWithAuthors;
  }

  private Optional<Book> findByIsbn(String isbn) {
    return bookRepository.findBookByIsbnInSet(isbn);
  }

  @Transactional
  public List<Book> findAll() {
    return bookRepository.findAll();
  }
}