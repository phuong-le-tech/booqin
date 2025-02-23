package fr.uge.booqIn.service;


import fr.uge.booqIn.dto.book.BookDTO;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
      this.authorRepository = authorRepository;
    }

    @Transactional
    public void save(Book toSave) {
        Objects.requireNonNull(toSave);
        boolean b = toSave.getIsbns().stream().filter(e -> findByIsbn(e).isPresent()).toList().isEmpty();
        if(!b){
            return;
        }
        for (Author author : toSave.getAuthors()) {
            if (author.getId() == null) {
                authorRepository.save(author);
            }
        }
        bookRepository.save(toSave);
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
        return bookRepository.findBookByIsbnInSet(isbn);
    }

    public List<Book> findByPublishedYear(String publishedDate) {
        Objects.requireNonNull(publishedDate);
        return bookRepository.findByPublishedYear(publishedDate);
    }

    public List<Book> saveAll(Iterable<Book> books){
        return bookRepository.saveAll(books);
    }

    public BookDTO convertToDTO(Book book) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return new BookDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthors().stream().map(author -> author.getFirstName() + " " + author.getLastName()).toList(),
            String.join(", ", book.getIsbns()),
            book.getDescription(),
            book.getPublicationDate(),
            dateFormat.format(book.getCreatedAt()),
            dateFormat.format(book.getUpdatedAt())
        );
    }
    public List<Book> findAllWithAuthors(){
        return bookRepository.findAllWithAuthors();
    }
}
