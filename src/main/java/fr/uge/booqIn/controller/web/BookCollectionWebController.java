package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.service.BookCollectionService;
import fr.uge.booqIn.service.impl.BookServiceImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/collections")
public class BookCollectionWebController {
  private final BookCollectionService bookCollectionService;
  private final BookServiceImpl bookService;
  private final BookMapper bookMapper = new BookMapper();
  private final Logger logger = Logger.getLogger(BookCollectionWebController.class.getName());

  public BookCollectionWebController(BookCollectionService bookCollectionService, BookServiceImpl bookService) {
    this.bookCollectionService = bookCollectionService;
    this.bookService = bookService;
  }

  @GetMapping("/public")
  public String showPublicCollections(Model model) {
    List<BookCollection> collections = bookCollectionService.getPublicCollections();
    model.addAttribute("publicCollections", collections);
    return "collections/public";
  }

  @GetMapping()
  public String showUserCollections(Model model, @AuthenticationPrincipal User currentUser) {
    List<BookCollection> collections = bookCollectionService.getUserCollections(currentUser);
    List<BookCollection> userPublicCollections = collections.stream().filter(BookCollection::isPublic).toList();
    List<BookCollection> userPrivateCollections = collections.stream().filter(collection -> !collection.isPublic()).toList();
    model.addAttribute("collections", collections);
    model.addAttribute("userPublicCollections", userPublicCollections);
    model.addAttribute("userPrivateCollections", userPrivateCollections);
    return "collections/userCollections";
  }

  @GetMapping("/{id}")
  public String showCollectionDetails(@PathVariable Long id, Model model) {
    Optional<BookCollection> collection = bookCollectionService.findCollectionById(id);
    if (collection.isEmpty()) {
      System.out.println("Collection not found or empty");
      return "redirect:/collections"; // Rediriger si la collection n'est pas trouvée
    }
    System.out.println("Collection size: " + collection.get().getBooks().size());
    System.out.println("Collection: \n" + collection.get());
    model.addAttribute("collection", collection.get());
    return "collections/details";
  }

  @GetMapping("/{collectionId}/books/add")
  public String showAddBookForm(@PathVariable Long collectionId, Model model) {
    Optional<BookCollection> bookCollection = bookCollectionService.findCollectionById(collectionId);
    if (bookCollection.isEmpty()) {
      System.out.println("Collection not found or empty");
      return "redirect:/collections"; // Rediriger si la collection n'est pas trouvée
    }
    //List<Book> books = bookCollection.get().getBooks();
    // TODO: A REMPLACER PAR LA GRILLE DE BOOKS AVEC PAGINATION ???
    //  PAGINER PAR ALPHABET ??
    List<Book> books = bookService.findAll().stream().limit(10).toList();

    model.addAttribute("collectionId", collectionId);
    model.addAttribute("collection", bookCollection.get());
    model.addAttribute("books", books);
    return "collections/addBook";
  }

  @PostMapping("/{collectionId}/books/add")
  public String addBookToCollection(
          @PathVariable Long collectionId,
          @RequestParam Long bookId,
          @AuthenticationPrincipal User currentUser,
          Model model
  ) {
    logger.info("Adding book with ID " + bookId + " to collection with ID " + collectionId);
    Optional<BookCollection> collectionOpt = bookCollectionService.findCollectionById(collectionId);
    if (collectionOpt.isPresent()) {
      logger.info("Collection found: " +  collectionOpt.get().getTitle());
      BookCollection collection = collectionOpt.get();
      //Optional<Book> bookOpt = bookCollectionService.getBookFromCollection(collectionId, bookId);
      //Optional<Book> bookOpt = bookService.findById(bookId);
      BookShow bookShow = bookService.findById(bookId);
      Set<Author> authors = bookShow.authors().stream().map(bookMapper::toAuthor).collect(Collectors.toSet());
      Optional<Book> bookOpt = Optional.of(new Book.Builder().id(bookShow.id()).isbns(bookShow.isbns())
              .title(bookShow.title()).authors(authors).description(bookShow.description())
              .publicationDate(bookShow.publicationDate()).createdAt(bookShow.createdAt())
              .updatedAt(bookShow.updatedAt())
              .build());
      if (bookOpt.isPresent()) {
        logger.info("Book found: " + bookOpt.get().getTitle());
        Book book = bookOpt.get();
        bookCollectionService.addBookToCollection(collectionId, book, currentUser);
        return "redirect:/collections/" + collectionId;
      } else {
        logger.warning("Book not found with ID: " + bookId);
        model.addAttribute("error", "Book not found");
      }
    } else {
      logger.warning("Collection not found with ID: " + collectionId);
      model.addAttribute("error", "Collection not found");
    }
    return "collections/addBook";
  }
}
