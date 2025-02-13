package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class BookRepositoryImpl {

    private final BookRepository bookRepository;

    @PersistenceUnit
    private final EntityManagerFactory emf;

    @PersistenceContext
    private final EntityManager em;


    public BookRepositoryImpl(BookRepository bookRepository, EntityManagerFactory emf, EntityManager em) {
        this.bookRepository = bookRepository;
        this.emf = emf;
        this.em = em;
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public void delete(Book book) {
        bookRepository.delete(book);
    }

    public List<Book> findByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    public List<Book> findByAuthors(String author) {
        return bookRepository.findByAuthors(author);
    }

    // Todo: Implement this method with Entity Manager
    public Optional<Book> findByIdIsbn(String isbn) {
        return Optional.empty();
    }

    public List<Book> findByPublishedYear(String publishedDate) {
        return bookRepository.findByPublishedYear(publishedDate);
    }
}
