package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.book.BookDto;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class BookService {
    private static final Logger logger = Logger.getLogger(BookService.class.getName());

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.bookMapper = bookMapper;
    }

    @Transactional
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Transactional
    public Book save(Book toSave) {
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


    @Transactional
    public void delete(Book book) {
        Objects.requireNonNull(book, "Le livre à supprimer ne peut pas être null");
        bookRepository.delete(book);
    }


    public Optional<Book> findById(Long id) {
        Objects.requireNonNull(id, "L'ID ne peut pas être null");
        return bookRepository.findById(id);
    }


    public List<Book> findAll() {
        return bookRepository.findAll();
    }


    public List<Book> findBooksByTitle(String title) {
        Objects.requireNonNull(title, "Le titre ne peut pas être null");
        return bookRepository.findBooksByTitle(title);
    }


    public List<Book> findBooksByAuthor(String author) {
        Objects.requireNonNull(author, "L'auteur ne peut pas être null");
        return bookRepository.findBooksByAuthor(author);
    }


    public Optional<Book> findByIsbn(String isbn) {
        Objects.requireNonNull(isbn, "L'ISBN ne peut pas être null");
        return bookRepository.findBookByIsbnInSet(isbn);
    }


    public List<Book> findByPublishedYear(String publishedDate) {
        Objects.requireNonNull(publishedDate, "La date de publication ne peut pas être null");
        return bookRepository.findByPublishedYear(publishedDate);
    }


    @Transactional
    public List<Book> saveAll(Iterable<Book> books) {
        Objects.requireNonNull(books, "La liste des livres ne peut pas être null");
        return bookRepository.saveAll(books);
    }


    @Transactional
    public List<BookDto> findAllDto() {
        List<Book> books = bookRepository.findAll();
        return bookMapper.toDtoList(books);
    }


    @Transactional
    public List<BookDto> findBooksByTitleDto(String title) {
        Objects.requireNonNull(title, "Title cannot be null");
        List<Book> books = bookRepository.findBooksByTitleWithAuthors(title);
        return bookMapper.toDtoList(books);
    }


    @Transactional
    public List<BookDto> findBooksByAuthorDto(String author) {
        Objects.requireNonNull(author, "Author cannot be null");
        List<Book> books = bookRepository.findBooksByAuthor(author);
        return bookMapper.toDtoList(books);
    }


    @Transactional
    public Optional<BookDto> findByIsbnDto(String isbn) {
        Objects.requireNonNull(isbn, "ISBN cannot be null");
        Optional<Book> bookOptional = bookRepository.findBookByIsbnInSet(isbn);
        return bookOptional.map(bookMapper::toDto);
    }


    @Transactional
    public BookDto saveDto(Book book) {
        Objects.requireNonNull(book, "Book cannot be null");
        Book savedBook = save(book);
        if (savedBook == null) {
            return null;
        }
        return bookMapper.toDto(savedBook);
    }


    @Transactional
    public List<Book> findAllWithAuthors() {
        return bookRepository.findAllWithAuthors();
    }
}