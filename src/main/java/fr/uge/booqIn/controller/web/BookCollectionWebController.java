package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.service.BookCollectionService;
import fr.uge.booqIn.service.impl.BookServiceImpl;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/collections")
public class BookCollectionWebController {
//  private final BookCollectionService bookCollectionService;
  private final BookCollectionServiceInterface bookCollectionService;
  private final BookServiceImpl bookService;
  private final BookMapper bookMapper = new BookMapper();
  private final BookCollectionMapper bookCollectionMapper = new BookCollectionMapper();
  private final Logger logger = Logger.getLogger(BookCollectionWebController.class.getName());

  public BookCollectionWebController(BookCollectionServiceInterface bookCollectionService, BookServiceImpl bookService) {
    this.bookCollectionService = bookCollectionService;
    this.bookService = bookService;
  }

  @GetMapping("/public")
  public String showPublicCollections(Model model) {
    //List<BookCollection> collections = bookCollectionService.getPublicCollections();
    List<BookCollectionShow> collections = bookCollectionService.getPublicCollections();
    model.addAttribute("publicCollections", collections);
    return "collections/public";
  }

  @GetMapping()
  public String showUserCollections(Model model, @AuthenticationPrincipal User currentUser) {
    List<BookCollectionShow> collections = bookCollectionService.getUserCollectionsWithBooks(currentUser);
    List<BookCollectionShow> userPublicCollections = collections.stream().filter(BookCollectionShow::isPublic).toList();
    List<BookCollectionShow> userPrivateCollections = collections.stream().filter(collection -> !collection.isPublic()).toList();
    model.addAttribute("collections", collections);
    model.addAttribute("userPublicCollections", userPublicCollections);
    model.addAttribute("userPrivateCollections", userPrivateCollections);
    return "collections/userCollections";
  } // TODO: rajouter pagination pour les collections, et pour le contenu d'une collection

  @GetMapping("/{id}")
  public String showCollectionDetails(@PathVariable Long id, Model model) {
    try {
      BookCollectionShow collection = bookCollectionService.findById(id);
      System.out.println("BookCollection was found !");
      System.out.println("BookCollection: " + collection);
      System.out.println("Collection size: " + collection.books().size());
      System.out.println("Collection: \n" + collection);
      model.addAttribute("collection", collection);
      return "collections/details";
    } catch (ResourceNotFoundException e) {
      System.out.println("Collection not found: " + e.getMessage());
      return "redirect:/collections";
    }
  }

  @GetMapping("/{collectionId}/books/add")
  public String showAddBookForm(@PathVariable Long collectionId, Model model) {
    BookCollectionShow bookCollection = bookCollectionService.findById(collectionId);
    try {
      // TODO: A REMPLACER PAR LA GRILLE DE BOOKS AVEC PAGINATION ???
      //  PAGINER PAR ALPHABET ??
      List<Book> books = bookService.findAll().stream().limit(10).toList();
      model.addAttribute("collectionId", collectionId);
      model.addAttribute("collection", bookCollection);
      model.addAttribute("books", books);
      return "collections/addBook";
    } catch (ResourceNotFoundException e) {
      System.out.println("Collection not found or empty");
      return "redirect:/collections"; // Rediriger si la collection n'est pas trouvée
    }
  }

  @PostMapping("/{collectionId}/books/add")
  public String addBookToCollection(
          @PathVariable Long collectionId,
          @RequestParam Long bookId,
          @AuthenticationPrincipal User currentUser,
          Model model
  ) {
    logger.info("Adding book with ID " + bookId + " to collection with ID " + collectionId);
    try {
      BookCollectionShow collectionOpt = bookCollectionService.findById(collectionId);
      logger.info("Collection found: " +  collectionOpt.name());
      BookShow bookShow = bookService.findById(bookId);
      Set<Author> authors = bookShow.authors().stream().map(bookMapper::toAuthor).collect(Collectors.toSet());
      Book book = new Book.Builder().id(bookShow.id())
              .title(bookShow.title()).authors(authors).description(bookShow.description())
              .publicationDate(bookShow.publicationDate()).createdAt(bookShow.createdAt())
              .updatedAt(bookShow.updatedAt())
              .build();
      bookCollectionService.addBookToCollection(collectionId, book.getId());
      return "redirect:/collections/" + collectionId;
    } catch (ResourceNotFoundException e) {
      logger.warning("Resource not found: " + e.getMessage());
      model.addAttribute("error", e.getMessage());
      return "collections/addBook";
    }
  }

  @GetMapping("/new")
  public String showCreateCollectionForm(Model model) {
    model.addAttribute("bookCollection", new BookCollection());
    return "collections/create";
  }

  @PostMapping("/new") // TODO: changer le request body par un DTO
  public String createCollection(
          @Valid @ModelAttribute("bookCollection") BookCollection bookCollection,
          @RequestParam(required = false) Boolean isPublic,
          @AuthenticationPrincipal User currentUser,
          Model model) {
    try {
      bookCollection.setOwner(currentUser);
      bookCollection.setPublic(isPublic != null && isPublic);
      BookCollection createdCollection = bookCollectionService.createBookCollection(bookCollection);
      System.out.println("Collection created: " + createdCollection);
      return "redirect:/collections";
    } catch (ResourceNotFoundException e) {
      model.addAttribute("error", e.getMessage());
      return "collections/create";
    }
  }

  @GetMapping("/{id}/edit")
  public String showEditCollectionForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User currentUser) {
    try {
      BookCollectionShow collection = bookCollectionService.findById(id);
      if (!verifyIsSameUser(currentUser, collection)) {
        return "redirect:/collections?error=notOwner";
      }

      // Convert DTO to entity for form binding
      BookCollection bookCollection = bookCollectionMapper.bookCollectionShowToBookCollection(collection);
      model.addAttribute("bookCollection", bookCollection);
      return "collections/edit";
    } catch (ResourceNotFoundException e) {
      return "redirect:/collections?error=notFound";
    }
  }

  @PostMapping("/{id}/edit")
  public String updateCollection(
          @PathVariable Long id,
          @Valid @ModelAttribute("bookCollection") BookCollection bookCollection,
          BindingResult bindingResult,
          @RequestParam(required = false) Boolean isPublic,
          @AuthenticationPrincipal User currentUser,
          Model model) {
    if (bindingResult.hasErrors()) {
      return "collections/edit";
    }

    try {
      BookCollectionShow existingCollection = bookCollectionService.findById(id);
      // Verify that the current user owns the collection
      if (!verifyIsSameUser(currentUser, existingCollection)) {
        return "redirect:/collections?error=notOwner";
      }

      // Update only allowed fields
      bookCollection.setId(id);
      bookCollection.setOwner(currentUser);
      bookCollection.setPublic(isPublic != null && isPublic);

      BookCollection updatedCollection = bookCollectionService.updateBookCollection(bookCollection);
      return "redirect:/collections";
    } catch (ResourceNotFoundException e) {
      model.addAttribute("error", e.getMessage());
      return "collections/edit";
    }
  }

  @GetMapping("/{id}/delete")
  public String deleteCollection(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
    try {
      // Verify that the current user owns the collection
      BookCollectionShow collection = bookCollectionService.findById(id);
      if (!verifyIsSameUser(currentUser, collection)) {
        return "redirect:/collections?error=notOwner";
      }

      // Delete the collection
      bookCollectionService.deleteBookCollection(id);
      return "redirect:/collections?success=deleted";
    } catch (ResourceNotFoundException e) {
      return "redirect:/collections?error=notFound";
    }
  }

  private boolean verifyIsSameUser(User currentUser, BookCollectionShow collection) {
    return Objects.equals(collection.user().id(), currentUser.getId());
  }
}
