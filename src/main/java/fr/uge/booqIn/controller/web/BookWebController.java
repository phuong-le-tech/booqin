package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookCreate;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.LiteCollection;
import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.dto.vote.VoteResponse;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.CommentServiceInterface;
import fr.uge.booqIn.service.interfaces.VoteServiceInterface;
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

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
public class BookWebController {
    private final BookServiceInterface bookService;
    private final VoteServiceInterface voteServiceInterface;
    private final BookCollectionServiceInterface bookCollectionService;
    private final CommentServiceInterface commentService;


    public BookWebController(BookServiceInterface bookService, VoteServiceInterface voteServiceInterface, BookCollectionServiceInterface bookCollectionService, CommentServiceInterface commentService) {
        this.bookService = bookService;
        this.voteServiceInterface = voteServiceInterface;
      this.bookCollectionService = bookCollectionService;
      this.commentService = commentService;
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
                              @AuthenticationPrincipal User currentUser) {

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
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        if(currentUser != null) {
            model.addAttribute("userId", currentUser.getId());
            var b = bookCollectionService.getUserCollections(currentUser);
            var bcc = b.stream().map(bc -> new LiteCollection(bc.getId(),bc.getTitle())).toList();
            model.addAttribute("bookCollections", bcc);
        }

        return "book/list";
    }

    @GetMapping("/{id}")
    public String getBookFromId(Model model, @PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        BookShow book = bookService.findById(id);
        model.addAttribute("book", book);
        VoteCount count = voteServiceInterface.getVoteCounts(id, "BOOK");
        var totalComments = commentService.countByEntity(id,"BOOK");
        var comments = commentService.getCommentTreeByEntity(id,"BOOK");

        model.addAttribute("votes", count);
        model.addAttribute("comments", comments);     // Add this line
        model.addAttribute("commentCount", totalComments);  // Add this line

        if (currentUser != null) {
            var voteUser = voteServiceInterface.getUserVoteType(currentUser.getId(),id,"BOOK");
            if(voteUser != null) {
                model.addAttribute("userVote",voteUser.toString());
            }
            model.addAttribute("user",currentUser);
        }

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

    @PostMapping("/vote")
    public String vote(@RequestParam("bookId") Long bookId,
                       @RequestParam("voteType") VoteType voteType,
                       @AuthenticationPrincipal User currentUser) {
        if (currentUser != null) {
            VoteCreate voteCreate = new VoteCreate(currentUser.getId(), bookId, "BOOK", voteType);
            voteServiceInterface.castVote(voteCreate);
        }
        return "redirect:/books/" + bookId;
    }

}