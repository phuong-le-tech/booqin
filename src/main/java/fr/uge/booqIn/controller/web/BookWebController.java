package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/books")
public class BookWebController {
    private final BookService bookService;
    public BookWebController(BookService bookService){
        this.bookService = bookService;
    }

    @GetMapping
    public String getAllBooks(Model model, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size",defaultValue = "120") int size){
        Page<Book> booksPage = bookService.findAll(PageRequest.of(page, size));
        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("currentPage", page);
        return "book/list";
    }
}