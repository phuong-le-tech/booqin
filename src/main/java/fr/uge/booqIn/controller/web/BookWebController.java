package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.service.BookService;
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
public String getAllBooks(Model model){
    List<Book> books = bookService.findAll();
    model.addAttribute("books",books);
    return "book/list";
}
}