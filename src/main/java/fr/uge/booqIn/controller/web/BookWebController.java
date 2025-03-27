
package fr.uge.booqIn.controller.web;
import fr.uge.booqIn.dto.book.BookCreate;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.LiteCollection;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.enums.LendStatus;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.model.BookCollection;
import fr.uge.booqIn.service.impl.UserVoteService;
import fr.uge.booqIn.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
public class BookWebController {

    private final BookServiceInterface bookService;
    private final VoteServiceInterface voteServiceInterface;
    private final UserServiceInterface userService;
    private final BookCollectionServiceInterface bookCollectionService;
    private final CommentServiceInterface commentService;
    private final HomeWebController homeWebController;
    private final UserVoteService userVoteService;
    private final BookLendingInterface bookLendingInterface;

    @Autowired
    public BookWebController(BookServiceInterface bookService,
                             VoteServiceInterface voteServiceInterface,
                             BookCollectionServiceInterface bookCollectionService,
                             UserServiceInterface userService,
                             UserServiceInterface userService,
                             CommentServiceInterface commentService,
                             HomeWebController homeWebController, UserVoteService userVoteService) {
                             HomeWebController homeWebController,
                             BookLendingInterface bookLendingInterface) {
        this.bookService = bookService;
        this.voteServiceInterface = voteServiceInterface;
        this.bookCollectionService = bookCollectionService;
        this.userService = userService;
        this.commentService = commentService;
        this.homeWebController = homeWebController;
      this.userVoteService = userVoteService;
      this.bookLendingInterface = bookLendingInterface;
    }

    @GetMapping
    public String getAllBooks(Model model,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "20") int size,
                              @RequestParam(value = "sort", defaultValue = "title") String sort,
                              @RequestParam(value = "direction", defaultValue = "asc") String direction,
                              @RequestParam(value = "query", required = false) String query,
                              @AuthenticationPrincipal User currentUser,
                              Principal principal) {

        // Préparer le modèle commun
        homeWebController.prepareModel(model, principal, "books");

        // Configurer la pagination et le tri
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // Recherche unifiée si query est fourni, sinon recherche classique
        Page<BookIndex> booksPage;
        if (query != null && !query.trim().isEmpty()) {
            booksPage = bookService.findBooks(query, query, query, pageable);
        } else {
            booksPage = bookService.findBooks(null, null, null, pageable);
        }

        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("query", query);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        if(currentUser != null) {
          model.addAttribute("userId", currentUser.getId());
          var user = userService.findUserById(currentUser.getId());
          List<LiteCollection> collectionShows = user.getCollections().stream().filter(collection -> collection.getCollectionType() != BookCollection.CollectionType.GLOBAL).map(c  -> new LiteCollection(c.getId(),c.getTitle())).toList();
          System.out.println(collectionShows.size());
          model.addAttribute("bookCollections", collectionShows);

          try {
              List<Long> availableBooks = new ArrayList<>();
              for (BookIndex book :booksPage.getContent()) {
                  Optional<BookLending> availableBook = bookLendingInterface.findByBookIdAndBorrowerIsNullAndStatus(book.id());
                  if (availableBook.isPresent()) {
                      availableBooks.add(book.id());
                  }
              }
              model.addAttribute("availableBooks", availableBooks);
            LendingResponseDTO lendingData = bookLendingInterface.findAllUserLendingsAsDTO(currentUser.getId(), LendStatus.ALL);
            List<Long> lentBooks = lendingData.lentBooks().stream()
                    .filter(lending -> lending.status() == BookStatus.AVAILABLE)
                    .map(lending -> lending.book().id())
                    .collect(Collectors.toList());
            List<Long> borrowedBooks = lendingData.lentBooks().stream()
                    .filter(lending -> lending.status() == BookStatus.BORROWED)
                    .map(lending -> lending.book().id())
                    .collect(Collectors.toList());
            model.addAttribute("lentBooks", lentBooks);
            model.addAttribute("borrowedBooks", borrowedBooks);
            System.out.println("Lending info added to model: lentBooks=" + lentBooks.size() + ", borrowedBooks=" + borrowedBooks.size());
          } catch (Exception e) {
            model.addAttribute("lentBooks", Collections.emptyList());
            model.addAttribute("borrowedBooks", Collections.emptyList());
          }
        }

        // Définir le titre et le template de contenu
        model.addAttribute("title", "BooqIn - Books");
        model.addAttribute("content", "pages/book/list");

        return "layout";
    }

    @GetMapping("/{id}")
    public String getBookFromId(Model model,
                                @PathVariable Long id,
                                @AuthenticationPrincipal User currentUser,
                                Principal principal) {
        // Préparer le modèle commun
        homeWebController.prepareModel(model, principal, "book-details");

        BookShow book = bookService.findById(id);
        model.addAttribute("book", book);
        VoteCount count = voteServiceInterface.getVoteCounts(id, "BOOK");
        var totalComments = commentService.countByEntity(id,"BOOK");
        var comments = commentService.getCommentTreeByEntity(id,"BOOK");

        model.addAttribute("votes", count);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", totalComments);
        model.addAttribute("entityType", "BOOK"); // Ajout pour le formulaire de commentaire
        model.addAttribute("entityId", id); // Ajout pour le formulaire de commentaire
        if (currentUser != null) {
            List<CommentDTO> list = commentService.getCommentTreeByEntity(id, "BOOK");
            Map<Long, VoteType> commentVotes = userVoteService.collectCommentVotes(currentUser.getId(), list);
            model.addAttribute("userCommentVotes", commentVotes);
            var voteUser = voteServiceInterface.getUserVoteType(currentUser.getId(),id,"BOOK");
            if(voteUser != null) {
                model.addAttribute("userVote",voteUser.toString());
            }
            model.addAttribute("user",currentUser);
        }

        // Définir le titre et le template de contenu
        model.addAttribute("title", "BooqIn - " + book.title());
        model.addAttribute("content", "pages/book/bookDetails");

        return "layout";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Principal principal) {
        // Préparer le modèle commun
        homeWebController.prepareModel(model, principal, "book-create");

        model.addAttribute("book", new BookCreate());

        // Définir le titre et le template de contenu
        model.addAttribute("title", "BooqIn - Add Book");
        model.addAttribute("content", "pages/book/bookCreate");

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
    
    @PostMapping("/{bookId}/delete")
    public String deleteBook(@PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return "redirect:/books";
    }
}
}
