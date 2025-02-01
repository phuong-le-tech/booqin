package fr.uge.booqIn.controller;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    public BookController() {
        this.bookService = new BookService("google");
    }

    @GetMapping("/searchByTitle")
    public List<Book> searchByTitle(@RequestParam String title) {
        System.out.println("RECHERCHE PAR TITRE " + title);
        return bookService.searchByTitle(title);
    }

    @GetMapping("/searchByAuthor")
    public List<Book> searchByAuthor(@RequestParam String author) {
        return bookService.searchByAuthor(author);
    }

    @GetMapping("/searchByIsbn")
    public List<Book> searchByIsbn(@RequestParam String isbn) {
        return bookService.searchByIsbn(isbn);
    }

    @GetMapping("/searchByPublisher")
    public List<Book> searchByPublisher(@RequestParam String publisher) {
        return bookService.searchByPublisher(publisher);
    }

    @GetMapping("/searchByPublishedDate")
    public List<Book> searchByPublishedDate(@RequestParam String publishedDate) {
        return bookService.searchByPublishedDate(publishedDate);
    }
}