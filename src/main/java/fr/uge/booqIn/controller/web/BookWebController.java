package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookCreate;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
public class BookWebController {
    private final BookServiceInterface bookService;
    private final HomeWebController homeWebController;

    @Autowired
    public BookWebController(BookServiceInterface bookService, HomeWebController homeWebController) {
        this.bookService = bookService;
        this.homeWebController = homeWebController;
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
                              Authentication authentication,
                              Principal principal) {

        // Préparer le modèle commun
        homeWebController.prepareModel(model, principal, "books");

        // Configurer la pagination et le tri
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // Récupérer les livres
        Page<BookIndex> booksPage = bookService.findBooks(title, author, isbn, pageable);

        // Ajouter les données au modèle
        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("title", title);
        model.addAttribute("author", author);
        model.addAttribute("isbn", isbn);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        if(authentication != null && authentication.isAuthenticated()){
            User user = (User) authentication.getPrincipal();
            model.addAttribute("userId", user.getId());
        }

        model.addAttribute("content", "pages/book/list"); // Le chemin actuel de votre template

        // Retourner le layout principal
        return "layout";
    }

    @GetMapping("/{id}")
    public String getBookFromId(Model model, @PathVariable Long id, Principal principal) {
        // Préparer le modèle commun
        homeWebController.prepareModel(model, principal, "book-details");

        // Récupérer les détails du livre
        BookShow book = bookService.findById(id);
        model.addAttribute("book", book);

        // Définir le titre de la page et le template de contenu
        model.addAttribute("title", "BooqIn - " + book.title());
        model.addAttribute("content", "pages/book/bookDetails");

        // Retourner le layout principal
        return "layout";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Principal principal) {
        // Préparer le modèle commun
        homeWebController.prepareModel(model, principal, "book-create");

        model.addAttribute("book", new BookCreate());

        // Définir le titre de la page et le template de contenu
        model.addAttribute("title", "BooqIn - Create Book");
        model.addAttribute("content", "pages/book/bookCreate");

        // Retourner le layout principal
        return "layout";
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