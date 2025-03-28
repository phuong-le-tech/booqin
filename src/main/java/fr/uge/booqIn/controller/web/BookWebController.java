
package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookCreate;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.LiteCollection;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.enums.LendStatus;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.interfaces.BookCollection;
import fr.uge.booqIn.service.impl.UserVoteService;
import fr.uge.booqIn.service.interfaces.*;
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
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
public class BookWebController {

    private static final Logger logger = Logger.getLogger(BookWebController.class.getName());

    private final BookServiceInterface bookService;
    private final VoteServiceInterface voteServiceInterface;
    private final UserServiceInterface userService;
    private final CommentServiceInterface commentService;
    private final FollowServiceInterface followService;
    private final HomeWebController homeWebController;
    private final UserVoteService userVoteService;
    private final BookLendingInterface bookLendingInterface;
    private final BookWaitingQueueServiceInterface bookWaitingQueueService;
    private static final String BOOK_TYPE="BOOK";
    private static final String TITLE="title";


    public BookWebController(BookServiceInterface bookService,
                             VoteServiceInterface voteServiceInterface,
                             UserServiceInterface userService,
                             CommentServiceInterface commentService,
                              FollowServiceInterface followService,
                             HomeWebController homeWebController,
                             UserVoteService userVoteService,
                             BookLendingInterface bookLendingInterface,
                             BookWaitingQueueServiceInterface bookWaitingQueueService) {
        this.bookService = bookService;
        this.voteServiceInterface = voteServiceInterface;
        this.userService = userService;
        this.commentService = commentService;
        this.followService = followService;
        this.homeWebController = homeWebController;
        this.userVoteService = userVoteService;
        this.bookLendingInterface = bookLendingInterface;
        this.bookWaitingQueueService = bookWaitingQueueService;
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
                              @AuthenticationPrincipal User currentUser,
                              Principal principal) {

        // Prepare common model
        homeWebController.prepareModel(model, principal, "books");

        // Configure pagination and sorting
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // Unified search if query provided, otherwise regular search
        Page<BookIndex> booksPage;
        if (isnotNullAndNotEmpty(title) ||  isnotNullAndNotEmpty(author) ||  isnotNullAndNotEmpty(isbn)) {
            booksPage = bookService.findBooks(title, author, isbn, pageable);
        } else {
            booksPage = bookService.findBooks(null, null, null, pageable);
        }

        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        if(currentUser != null) {
            model.addAttribute("userId", currentUser.getId());
            var user = userService.findUserById(currentUser.getId());
            model.addAttribute("bookCollections", getLiteCollections(user));

            try {
                // Track available books
                List<Long> availableBooks = new ArrayList<>();
                // Track books in the waiting queue and their positions
                Map<Long, Integer> bookQueuePositions = new HashMap<>();

                // Check availability for all displayed books
                for (BookIndex book : booksPage.getContent()) {
                    Optional<BookLending> availableBook = bookLendingInterface.findByBookIdAndBorrowerIsNullAndStatus(book.id());
                    if (availableBook.isPresent()) {
                        availableBooks.add(book.id());
                    }

                    // Check queue positions for each book
                    int position = bookWaitingQueueService.getUserQueuePosition(book.id(), currentUser.getId());
                    if (position > 0) {
                        bookQueuePositions.put(book.id(), position);
                    }
                }

                model.addAttribute("availableBooks", availableBooks);
                model.addAttribute("bookQueuePositions", bookQueuePositions);

                // Get lending information
                LendingResponseDTO lendingData = bookLendingInterface.findAllUserLendingsAsDTO(currentUser.getId(), LendStatus.ALL);
                List<Long> lentBooks = getLentBooks(lendingData, BookStatus.AVAILABLE);
                List<Long> borrowedBooks = getLentBooks(lendingData, BookStatus.BORROWED);
                model.addAttribute("lentBooks", lentBooks);
                model.addAttribute("borrowedBooks", borrowedBooks);

                // Get user's active notifications
                List<BookNotificationDTO> activeQueues = bookWaitingQueueService.getUserActiveQueues(currentUser.getId());
                model.addAttribute("activeQueues", activeQueues);

                // Create a map of notification IDs by book ID for easy access
                Map<Long, Long> notificationIdsByBook = activeQueues.stream()
                        .collect(Collectors.toMap(
                                BookNotificationDTO::bookId,
                                BookNotificationDTO::id,
                                (existing, replacement) -> existing // Keep first in case of duplicates
                        ));
                model.addAttribute("notificationIdsByBook", notificationIdsByBook);
            } catch (Exception e) {
                logger.warning("Error loading lending data: " + e.getMessage());
                model.addAttribute("lentBooks", Collections.emptyList());
                model.addAttribute("borrowedBooks", Collections.emptyList());
                model.addAttribute("bookQueuePositions", Collections.emptyMap());
                model.addAttribute("activeQueues", Collections.emptyList());
                model.addAttribute("notificationIdsByBook", Collections.emptyMap());
            }
        }

        // Set title and content template
        model.addAttribute("TITLE", "BooqIn - Books");
        model.addAttribute("content", "pages/book/list");

        return "layout";
    }

    private boolean isnotNullAndNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private List<LiteCollection> getLiteCollections(User user) {
        List<LiteCollection> test = user.getCollections().stream()
                .filter(collection -> collection.getCollectionType() != BookCollection.CollectionType.GLOBAL
                        && collection.getCollectionType() != BookCollection.CollectionType.SMART
                        && collection.getCollectionType() != BookCollection.CollectionType.FOLLOWED
                        && collection.getCollectionType() != BookCollection.CollectionType.LIKED)
                .map(c -> new LiteCollection(c.getId(), c.getTitle()))
                .toList();
        System.out.println("Collections LITE: " + test);
        return test;
    }

    private List<Long> getLentBooks(LendingResponseDTO lendingData, BookStatus available) {
        return lendingData.lentBooks().stream()
            .filter(lending -> lending.status() == available)
            .map(lending -> lending.book().id())
            .toList();
    }

    @GetMapping("/{id}")
    public String getBookFromId(Model model,
                                @PathVariable Long id,
                                @AuthenticationPrincipal User currentUser,
                                Principal principal) {
        // Prepare common model
        homeWebController.prepareModel(model, principal, "book-details");

        BookShow book = bookService.findById(id);
        model.addAttribute("book", book);
        VoteCount count = voteServiceInterface.getVoteCounts(id, BOOK_TYPE);
        var totalComments = commentService.countByEntity(id,BOOK_TYPE);
        var comments = commentService.getCommentTreeByEntity(id,BOOK_TYPE);

        model.addAttribute("votes", count);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", totalComments);
        model.addAttribute("entityType", BOOK_TYPE); // Added for comment form
        model.addAttribute("entityId", id); // Added for comment form

        if (currentUser != null) {
            boolean isFollowing = followService.isFollowing(currentUser.getId(), id, "BOOK");
            model.addAttribute("isFollowing", isFollowing);

            // Get followers count
            long followersCount = followService.countFollowers(id, "BOOK");
            model.addAttribute("followersCount", followersCount);

            Map<Long, VoteType> commentVotes = userVoteService.collectCommentVotes(currentUser.getId(), comments);
            model.addAttribute("userCommentVotes", commentVotes);
            var voteUser = voteServiceInterface.getUserVoteType(currentUser.getId(),id,BOOK_TYPE);
            if(voteUser != null) {
                model.addAttribute("userVote",voteUser.toString());
            }
            model.addAttribute("user", currentUser);
            // Check book availability
            boolean isBookAvailable = false;
            Optional<BookLending> availableBook = bookLendingInterface.findByBookIdAndBorrowerIsNullAndStatus(id);
            if (availableBook.isPresent()) {
                isBookAvailable = true;
            }
            model.addAttribute("isBookAvailable", isBookAvailable);

            // Check queue position
            int queuePosition = bookWaitingQueueService.getUserQueuePosition(id, currentUser.getId());
            model.addAttribute("queuePosition", queuePosition);

            // Get notification ID if in queue
            if (queuePosition > 0) {
                List<BookNotificationDTO> activeQueues = bookWaitingQueueService.getUserActiveQueues(currentUser.getId());
                Optional<BookNotificationDTO> notification = activeQueues.stream()
                        .filter(queue -> Objects.equals(queue.bookId(), id))
                        .findFirst();

                notification.ifPresent(n -> model.addAttribute("notificationId", n.id()));
            }
        }

        // Set title and content template
        model.addAttribute(TITLE, "BooqIn - " + book.title());
        model.addAttribute("content", "pages/book/bookDetails");

        return "layout";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Principal principal) {
        // Préparer le modèle commun
        homeWebController.prepareModel(model, principal, "book-create");

        model.addAttribute("book", new BookCreate());

        // Définir le titre et le template de contenu
        model.addAttribute(TITLE, "BooqIn - Add Book");
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

        Set<Author> authorSet = getAuthors(authors);
        // Conversion en Set pour les ISBN et filtrage des valeurs vides
        Set<String> isbnSet = getIsbnSet(isbns);
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

    private Set<String> getIsbnSet(List<String> isbns) {
        return isbns.stream()
            .filter(isbn -> isbn != null && !isbn.trim().isEmpty())
            .collect(Collectors.toSet());
    }

    private Set<Author> getAuthors(List<String> authors) {
        return authors.stream()
            .filter(a -> a != null && !a.trim().isEmpty())
            .map(a -> a.split(" ", 2))
            .map(author -> new Author(author[0], author.length > 1 ? author[1] : ""))
            .collect(Collectors.toSet());
    }

    @PostMapping("/vote")
    public String vote(@RequestParam("bookId") Long bookId,
                       @RequestParam("voteType") VoteType voteType,
                       @AuthenticationPrincipal User currentUser) {
        if (currentUser != null) {
            VoteCreate voteCreate = new VoteCreate(currentUser.getId(), bookId, BOOK_TYPE, voteType);
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