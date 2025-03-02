package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.specification.BookSpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
  public BookShow findById(@NotNull Long id) {
    Book book = findBookById(id);
    return bookMapper.toBookShow(book);
  }

  public Book createBook(@NotNull @Valid Book book) {
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
  public Book updateBook(@NotNull @Valid Book book) {
    if (!bookRepository.existsById(book.getId())) {
      throw new ResourceNotFoundException("Book", "id", book.getId());
    }

    book.setUpdatedAt(new Date());
    return bookRepository.save(book);
  }

  @Override
  @Transactional
  public BookShow patchBook(@NotNull Long id, @NotNull @Valid BookUpdate bookUpdate) {
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
  public void deleteBook(@NotNull Long id) {
    Book book = findBookById(id);
    bookRepository.delete(book);
  }

  @Transactional
  public Book saveFromApi(Book toSave) {
    Objects.requireNonNull(toSave, "Le livre à sauvegarder ne peut pas être null");

    boolean isbnExists = toSave.getIsbns().stream()
            .anyMatch(isbn -> findByIsbn(isbn).isPresent());

    if (isbnExists) {
      logger.info("Un livre avec l'un des ISBNs existe déjà");
      return null;
    }

    if (toSave.getAuthors() == null) {
      toSave.setAuthors(new HashSet<>());
    }

    toSave.getAuthors().stream()
            .filter(author -> author.getId() == null)
            .forEach(authorRepository::save);

    return bookRepository.save(toSave);
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