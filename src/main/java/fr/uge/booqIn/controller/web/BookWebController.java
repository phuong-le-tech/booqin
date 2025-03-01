package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;

@Controller
@RequestMapping("/books")
public class BookWebController {
    private final BookServiceInterface bookService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public BookWebController(BookServiceInterface bookService){
        this.bookService = bookService;
    }

    @GetMapping
    public String getAllBooks(Model model,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "120") int size,
                              @RequestParam(value = "sort", defaultValue = "title") String sort,
                              @RequestParam(value = "direction", defaultValue = "asc") String direction,
                              @RequestParam(value = "title", required = false) String title,
                              @RequestParam(value = "author", required = false) String author,
                              @RequestParam(value = "isbn", required = false) String isbn) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(sortDirection, sort);

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<BookIndex> booksPage = bookService.findBooks(title, author, isbn, pageable);

        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("title", title);
        model.addAttribute("author", author);
        model.addAttribute("isbn", isbn);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "book/list";
    }
}