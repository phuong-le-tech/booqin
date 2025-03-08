package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookCreate;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.mapper.UserMapper;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
public class BookWebController {
    private final BookServiceInterface bookService;

    public BookWebController(BookServiceInterface bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String getAllBooks(Model model,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "20") int size,
                              @RequestParam(value = "sort", defaultValue = "title") String sort,
                              @RequestParam(value = "direction", defaultValue = "asc") String direction,
                              @RequestParam(value = "title", required = false) String title,
                              @RequestParam(value = "author", required = false) String author,
                              @RequestParam(value = "isbn", required = false) String isbn,
                              Authentication authenfication) {

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

        if(authenfication != null && authenfication.isAuthenticated()){
            UserMapper userMapper = new UserMapper();
            User user = (User) authenfication.getPrincipal();
            UserShow showUser = userMapper.toUserShow(user);
            model.addAttribute("userId",showUser.id());
        }

        return "book/list";
    }

    @GetMapping("/{id}")
    public String getBookFromId(Model model, @PathVariable Long id) {
        BookShow book = bookService.findById(id);
        model.addAttribute("book", book);
        return "book/bookDetails";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new BookCreate());
        return "book/bookCreate";
    }

    @PostMapping
    public String addBook(
        @RequestParam("title") String title,
        @RequestParam("authors") List<String> authors,
        @RequestParam("isbn") List<String> isbns,
        @RequestParam("publicationDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date publicationDate,
        @RequestParam("description") String description) {

        Set<Author> authorSet = authors.stream()
            .filter(a -> a != null && !a.trim().isEmpty())
            .map(a -> a.split(" "))
            .map(author -> {
                if (author.length > 1) {
                    return new Author(author[0], author[1]);
                } else {
                    return new Author(author[0], "");
                }
            })
            .collect(Collectors.toSet());

        // Conversion en Set pour les ISBN et filtrage des valeurs vides
        Set<String> isbnSet = isbns.stream()
            .filter(isbn -> isbn != null && !isbn.trim().isEmpty())
            .collect(Collectors.toSet());

        // Construction du livre
        Book book = new Book.Builder()
            .isbns(isbnSet)
            .title(title)
            .authors(authorSet)
            .description(description)
            .publicationDate(publicationDate)
            .build();

        bookService.createBook(book);
        return "redirect:/books";
    }
}