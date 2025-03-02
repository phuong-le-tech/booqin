package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookCreateDto;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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

    @GetMapping("/{id}")
    public String getBookFromId(Model model, @PathVariable Long id) {
        BookShow book = bookService.findById(id);
        model.addAttribute("book", book);
        return "book/bookDetails";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new BookCreateDto());
        return "book/bookCreate";
    }

    @PostMapping
    public String addBook(@ModelAttribute BookCreateDto bookCreateDto) {
        // Traitement du nom d'auteur (simple pour l'exemple)
        String authorName = bookCreateDto.author();
        String firstName = "";
        String lastName = authorName;

        // Si le nom contient un espace, on considère le format "Prénom Nom"
        if (authorName.contains(" ")) {
            String[] parts = authorName.split(" ", 2);
            firstName = parts[0];
            lastName = parts[1];
        }

        // Création d'un seul auteur
        Author author = new Author(firstName, lastName);
        Set<Author> authors = new HashSet<>();
        authors.add(author);

        // Création d'un seul ISBN
        Set<String> isbns = new HashSet<>();
        if (bookCreateDto.isbn() != null && !bookCreateDto.isbn().isEmpty()) {
            isbns.add(bookCreateDto.isbn());
        }

        // Construction du livre
        Book book = new Book.Builder()
                .isbns(isbns)
                .title(bookCreateDto.title())
                .authors(authors)
                .description(bookCreateDto.description())
                .publicationDate(bookCreateDto.publicationDate())
                .build();

        bookService.createBook(book);
        return "redirect:/books";
    }
}