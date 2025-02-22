package fr.uge.booqIn.controller;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.repository.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/searchByTitle")
    public List<Book> searchByTitle(@RequestParam String title) {
        System.out.println("RECHERCHE PAR TITRE " + title);
        return bookService.findBooksByTitle(title);
    }

    @GetMapping("/searchByAuthor")
    public List<Book> searchByAuthor(@RequestParam String author) {
        return bookService.findBooksByAuthor(author);
    }

    @GetMapping("/searchByIsbn")
    public Optional<Book> searchByIsbn(@RequestParam String isbn) {
        return bookService.findByIsbn(isbn);
    }
}