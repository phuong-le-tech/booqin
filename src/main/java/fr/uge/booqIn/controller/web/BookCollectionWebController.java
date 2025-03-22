package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.model.BookCollection;
import fr.uge.booqIn.service.impl.BookServiceImpl;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/collections")
public class BookCollectionWebController {
  private final BookCollectionServiceInterface bookCollectionService;
  private final BookServiceImpl bookService;
  private final BookMapper bookMapper = new BookMapper();
  private final BookCollectionMapper bookCollectionMapper = new BookCollectionMapper();
  private final Logger logger = Logger.getLogger(BookCollectionWebController.class.getName());
  private final HomeWebController homeWebController;
  private final BookLendingInterface bookLendingInterface;

  @Autowired
  public BookCollectionWebController(BookCollectionServiceInterface bookCollectionService,
                                     BookServiceImpl bookService,
                                     HomeWebController homeWebController,
                                     BookLendingInterface bookLendingInterface) {
    this.bookCollectionService = bookCollectionService;
    this.bookService = bookService;
    this.homeWebController = homeWebController;
    this.bookLendingInterface = bookLendingInterface;
  }

  @GetMapping("/public")
  public String showPublicCollections(Model model, Principal principal) {
    homeWebController.prepareModel(model, principal, "collections-public");
    // Récupérer les collections publiques
    Page<BookCollectionIndex> collections = bookCollectionService.getPublicCollections(null);
    model.addAttribute("publicCollections", collections);
    model.addAttribute("title", "BooqIn - Public Collections");
    model.addAttribute("content", "pages/collections/public");
    return "layout";
  }

  @GetMapping()
  public String showUserCollections(Model model, @AuthenticationPrincipal User currentUser, Principal principal) {
    homeWebController.prepareModel(model, principal, "collections");
   

    Page<BookCollectionShow> collections = bookCollectionService.getUserCollectionsWithBooks(currentUser, null);
    List<BookCollectionShow> userPublicCollections = collections.stream().filter(BookCollectionShow::isPublic).toList();
    List<BookCollectionShow> userPrivateCollections = collections.stream().filter(collection -> !collection.isPublic()).toList();
    model.addAttribute("collections", collections);
    model.addAttribute("userPublicCollections", userPublicCollections);
    model.addAttribute("userPrivateCollections", userPrivateCollections);

    // Définir le titre et le contenu
    model.addAttribute("title", "BooqIn - My Collections");
    model.addAttribute("content", "pages/collections/userCollections");

    return "layout";
  }

  @GetMapping("/{id}")
  public String showCollectionDetails(@PathVariable Long id, Model model, Principal principal, @AuthenticationPrincipal User currentUser) {
    try {
      homeWebController.prepareModel(model, principal, "collection-details");
      

      BookCollectionShow collection = bookCollectionService.findBookCollectionById(id);
      System.out.println("BookCollection was found !");
      model.addAttribute("collection", collection);
      model.addAttribute("currentUser", currentUser);
      boolean isOwner = (currentUser != null && Objects.equals(collection.user().id(), currentUser.getId()));
      model.addAttribute("isOwner", isOwner);
      if (currentUser != null) {
        try {
          LendingResponseDTO lendingData = bookLendingInterface.findAllUserLendingsAsDTO(currentUser.getId(), "all");
          StringBuilder logMsg = new StringBuilder();
          System.out.println("Lending data fetched: " + lendingData.lentBooks().size() + " lent books");
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
          logger.info("Lending info added to model: lentBooks=" + lentBooks.size() + ", borrowedBooks=" + borrowedBooks.size());
        } catch (Exception e) {
          model.addAttribute("lentBooks", Collections.emptyList());
          model.addAttribute("borrowedBooks", Collections.emptyList());
        }
      }
      

      model.addAttribute("title", "BooqIn - " + collection.title());
      model.addAttribute("content", "pages/collections/details");
      
      return "layout";
    } catch (ResourceNotFoundException e) {
      System.out.println("Collection not found: " + e.getMessage());
      return "redirect:/collections";
    }
  }


  @GetMapping("/{collectionId}/books/add")
  public String showAddBookForm(@PathVariable Long collectionId, Model model, Principal principal) {
    try {
      // Préparer le modèle commun
      homeWebController.prepareModel(model, principal, "collection-add-book");

      BookCollectionShow bookCollection = bookCollectionService.findBookCollectionById(collectionId);
      List<Book> books = bookService.findAll().stream().limit(10).toList();
      model.addAttribute("collectionId", collectionId);
      model.addAttribute("collection", bookCollection);
      model.addAttribute("books", books);

      // Définir le titre et le contenu
      model.addAttribute("title", "BooqIn - Add Book to Collection");
      model.addAttribute("content", "pages/collections/addBook");

      return "layout";
    } catch (ResourceNotFoundException e) {
      System.out.println("Collection not found or empty");
      return "redirect:/collections";
    }
  }

  @PostMapping("/{collectionId}/books/add")
  public String addBookToCollection(
          @PathVariable Long collectionId,
          @RequestParam Long bookId,
          @AuthenticationPrincipal User currentUser,
          Model model,
          Principal principal
  ) {
    logger.info("Adding book with ID " + bookId + " to collection with ID " + collectionId);
    try {
      BookCollectionShow collectionOpt = bookCollectionService.findBookCollectionById(collectionId);
      logger.info("Collection found: " +  collectionOpt.title());
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

      // Préparer le modèle commun même en cas d'erreur
      homeWebController.prepareModel(model, principal, "collection-add-book");

      model.addAttribute("error", e.getMessage());
      model.addAttribute("title", "BooqIn - Error");
      model.addAttribute("content", "pages/collections/addBook");

      return "layout";
    }
  }

  @GetMapping("/{collectionId}/books/{bookId}/remove")
  public String removeBookFromCollection(
          @PathVariable Long collectionId,
          @PathVariable Long bookId,
          @AuthenticationPrincipal User currentUser,
          Model model) {
    logger.info("Removing book with ID " + bookId + " from collection with ID " + collectionId);
    try {
      // Verify that the current user owns the collection
      BookCollectionShow collection = bookCollectionService.findBookCollectionById(collectionId);
      if (!verifyIsSameUser(currentUser, collection)) {
        return "redirect:/collections?error=notOwner";
      }

      // Remove the book from the collection
      bookCollectionService.removeBookFromCollection(collectionId, bookId);
      return "redirect:/collections/" + collectionId + "?success=bookRemoved";
    } catch (ResourceNotFoundException e) {
      logger.warning("Error removing book: " + e.getMessage());
      return "redirect:/collections/" + collectionId + "?error=notFound";
    }
  }

  @GetMapping("/new")
  public String showCreateCollectionForm(Model model, Principal principal) {
    // Préparer le modèle commun
    homeWebController.prepareModel(model, principal, "collection-create");

    model.addAttribute("bookCollection", null);

    // Définir le titre et le contenu
    model.addAttribute("title", "BooqIn - Create Collection");
    model.addAttribute("content", "pages/collections/create");

    return "layout";
  }

  /*@PostMapping("/new")
  public String createCollection(
          @Valid @ModelAttribute("bookCollection") BookCollectionCreate bookCollection,
          Model model,
          Principal principal) {
    try {
      bookCollection.setOwner(currentUser);
      bookCollection.setPublic(isPublic != null && isPublic);
      BookCollection createdCollection = bookCollectionService.createBookCollection(bookCollection);
      System.out.println("Collection created: " + createdCollection);
      return "redirect:/collections";
    } catch (ResourceNotFoundException e) {
      // Préparer le modèle commun même en cas d'erreur
      homeWebController.prepareModel(model, principal, "collection-create");

      model.addAttribute("error", e.getMessage());
      model.addAttribute("title", "BooqIn - Error");
      model.addAttribute("content", "pages/collections/create");

      return "layout";
    }
  }*/

  @GetMapping("/{id}/edit")
  public String showEditCollectionForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User currentUser, Principal principal) {
    try {
      // Préparer le modèle commun
      homeWebController.prepareModel(model, principal, "collection-edit");

      BookCollectionShow collection = bookCollectionService.findBookCollectionById(id);
      if (!verifyIsSameUser(currentUser, collection)) {
        return "redirect:/collections?error=notOwner";
      }
      model.addAttribute("bookCollection", collection);

      // Définir le titre et le contenu
      model.addAttribute("title", "BooqIn - Edit Collection");
      model.addAttribute("content", "pages/collections/edit");

      return "layout";
    } catch (ResourceNotFoundException e) {
      model.addAttribute("error", e.getMessage());
      System.out.println("ERROR WHEN FORM EDIT");
      return "redirect:/collections?error=notFound";
    }
  }

  @PostMapping("/{id}/edit")
  public String updateCollection(
          @PathVariable Long id,
          @Valid @ModelAttribute("bookCollection") BookCollection bookCollection,
          BindingResult bindingResult,
          @RequestParam(name = "isPublic", required = false) Boolean isPublic,
          @AuthenticationPrincipal User currentUser,
          Model model,
          Principal principal) {
    if (bindingResult.hasErrors()) {
      System.out.println("BINDING ERRORS: " + bindingResult.getAllErrors());
      return "redirect:/collections?error=notFound";
    }
    try {
      BookCollectionShow existingCollection = bookCollectionService.findBookCollectionById(id);
      if (!verifyIsSameUser(currentUser, existingCollection)) {
        return "redirect:/pages/collections?error=notOwner";
      }

      System.out.println("isPublic: " + isPublic);
      // Update only allowed fields
      bookCollection.setId(id);
      bookCollection.setOwner(currentUser);
      bookCollection.setPublic(isPublic != null);

      BookCollectionUpdate bookCollectionUpdate = new BookCollectionUpdate(
              Optional.of(bookCollection.getTitle()),
              Optional.of(bookCollection.getDescription()),
              isPublic != null
      );
      BookCollectionShow updatedCollection = bookCollectionService.updateBookCollection(id, bookCollectionUpdate);
      return "redirect:/collections";
    } catch (ResourceNotFoundException e) {
      // Préparer le modèle commun même en cas d'erreur
      homeWebController.prepareModel(model, principal, "collection-edit");

      model.addAttribute("error", e.getMessage());
      model.addAttribute("title", "BooqIn - Error");
      model.addAttribute("content", "pages/collections/edit");

      return "layout";
    }
  }

  @GetMapping("/{id}/delete")
  public String deleteCollection(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
    try {
      // Verify that the current user owns the collection
      BookCollectionShow collection = bookCollectionService.findBookCollectionById(id);
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