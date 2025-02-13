package fr.uge.booqIn.repository;


import fr.uge.booqIn.entity.Book;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book save(Book toSave){
        Objects.requireNonNull(toSave);
        return bookRepository.save(toSave);
    }

    public void delete(Book book){
        Objects.requireNonNull(book);
        bookRepository.delete(book);
    }

    public Optional<Book> findById(Long id){
        return bookRepository.findById(id);
    }

    public List<Book> findAll(){
        return bookRepository.findAll();
    }

    public List<Book> findBooksByTitle(String title) {
        Objects.requireNonNull(title);
        return bookRepository.findBooksByTitle(title);
    }

    public List<Book> findBooksByAuthor(String author) {
        Objects.requireNonNull(author);
        return bookRepository.findBooksByAuthor(author);
    }

    public Optional<Book> findByIsbn(String isbn) {
        Objects.requireNonNull(isbn);
        return bookRepository.findByIsbn10OrIsbn13(isbn);
    }

    public List<Book> findByPublishedYear(String publishedDate) {
        Objects.requireNonNull(publishedDate);
        return bookRepository.findByPublishedYear(publishedDate);
    }

    public List<Book> saveAll(Iterable<Book> books){
        return bookRepository.saveAll(books);
    }

}
