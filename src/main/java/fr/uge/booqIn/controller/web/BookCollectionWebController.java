package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionCreate;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.enums.LendStatus;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.interfaces.BookCollection;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.service.impl.UserVoteService;
import fr.uge.booqIn.service.interfaces.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/collections")
public class BookCollectionWebController {
  private final BookCollectionServiceInterface bookCollectionService;
  private final VoteServiceInterface voteService;
  private final CommentServiceInterface commentServiceInterface;
  private final BookServiceInterface bookService;
  private final FollowServiceInterface followService;
  private final BookMapper bookMapper = new BookMapper();
  private final BookCollectionMapper bookCollectionMapper = new BookCollectionMapper();
  private final Logger logger = Logger.getLogger(BookCollectionWebController.class.getName());
  private final HomeWebController homeWebController;
  private final BookLendingInterface bookLendingInterface;
  private final UserVoteService userVoteService;
  private final CommentServiceInterface commentService;

  @Autowired
  public BookCollectionWebController(BookCollectionServiceInterface bookCollectionService,
                                     VoteServiceInterface voteService,
                                     CommentServiceInterface commentServiceInterface,
                                     BookServiceInterface bookService,
                                     FollowServiceInterface followService,
                                     HomeWebController homeWebController,
                                     BookLendingInterface bookLendingInterface,
                                     UserVoteService userVoteService,
                                     CommentServiceInterface commentService) {
    this.bookCollectionService = bookCollectionService;
    this.voteService = voteService;
    this.commentServiceInterface = commentServiceInterface;
    this.bookService = bookService;
    this.followService = followService;
    this.homeWebController = homeWebController;
    this.bookLendingInterface = bookLendingInterface;
    this.userVoteService = userVoteService;
    this.commentService = commentService;
  }

  @GetMapping("/public")
  public String showPublicCollections(Model model, Principal principal,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
    // Préparer le modèle commun
    homeWebController.prepareModel(model, principal, "collections-public");

    Page<BookCollectionIndex> collections = bookCollectionService.getPublicCollections(PageRequest.of(page, size));
    model.addAttribute("publicCollections", collections);
    model.addAttribute("pageSize", size);
    model.addAttribute("title", "BooqIn - Public Collections");
    model.addAttribute("content", "pages/collections/public");
    return "layout";
  }
  
  @GetMapping()
  public String showUserCollections(
          Model model,
          @AuthenticationPrincipal User currentUser,
          Principal principal,
          @RequestParam(value = "tab", required = false, defaultValue = "private") String activeTab,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "20") int size,
          @RequestParam(value = "sort", defaultValue = "title") String sort,
          @RequestParam(value = "direction", defaultValue = "asc") String direction
  ) {
    homeWebController.prepareModel(model, principal, "collections");
    
    // Définir l'onglet actif
    model.addAttribute("activeTab", activeTab);
    
    Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ?
            Sort.Direction.DESC : Sort.Direction.ASC;
    Sort sortObj = Sort.by(sortDirection, sort);
    
    // Créer différents objets Pageable pour chaque onglet
    Pageable privatePageable = PageRequest.of("private".equals(activeTab) ? page : 0, size, sortObj);
    Pageable publicPageable = PageRequest.of("public".equals(activeTab) ? page : 0, size, sortObj);
    Pageable smartPageable = PageRequest.of("smart".equals(activeTab) ? page : 0, size, sortObj);
    Pageable globalPageable = PageRequest.of("global".equals(activeTab) ? page : 0, size, sortObj);
    
    // Collections privées (type NORMAL uniquement)
    List<BookCollectionIndex> allUserPrivateCollections = bookCollectionService.getPrivateUserCollection(currentUser, Pageable.unpaged()).getContent();
    List<BookCollectionIndex> filteredPrivateCollections = allUserPrivateCollections.stream()
            .filter(collection -> collection.type() == BookCollection.CollectionType.NORMAL)
            .collect(Collectors.toList());
    
    // Collections publiques de l'utilisateur (type NORMAL uniquement)
    List<BookCollectionIndex> allUserPublicCollections = bookCollectionService.getPublicUserCollection(currentUser, Pageable.unpaged()).getContent();
    List<BookCollectionIndex> filteredPublicCollections = allUserPublicCollections.stream()
            .filter(collection -> collection.type() == BookCollection.CollectionType.NORMAL)
            .collect(Collectors.toList());
    
    // Collections smart (type SMART uniquement)
    List<BookCollectionIndex> filteredSmartCollections = allUserPrivateCollections.stream()
            .filter(collection -> collection.type() == BookCollection.CollectionType.SMART)
            .collect(Collectors.toList());
    
    // Collections globales (types AUTHOR et RECENT)
    List<BookCollectionIndex> allPublicSystemCollections = bookCollectionService.getPublicCollections(Pageable.unpaged()).getContent();
    List<BookCollectionIndex> filteredGlobalCollections = allPublicSystemCollections.stream()
            .filter(collection -> collection.type() == BookCollection.CollectionType.AUTHOR ||
                    collection.type() == BookCollection.CollectionType.RECENT)
            .collect(Collectors.toList());
    
    // Paginer les collections filtrées
    Page<BookCollectionIndex> privateCollections = bookCollectionService.createPaginatedResult(filteredPrivateCollections, privatePageable);
    Page<BookCollectionIndex> publicCollections = bookCollectionService.createPaginatedResult(filteredPublicCollections, publicPageable);
    Page<BookCollectionIndex> smartCollections = bookCollectionService.createPaginatedResult(filteredSmartCollections, smartPageable);
    Page<BookCollectionIndex> globalCollections = bookCollectionService.createPaginatedResult(filteredGlobalCollections, globalPageable);
    
    // Ajouter les collections paginées au modèle
    model.addAttribute("userPrivateCollections", privateCollections);
    model.addAttribute("userPublicCollections", publicCollections);
    model.addAttribute("smartCollections", smartCollections);
    model.addAttribute("globalCollections", globalCollections);
    
    // Ajouter les informations de pagination et tri
    model.addAttribute("sortField", sort);
    model.addAttribute("sortDirection", direction);
    model.addAttribute("page", page);
    model.addAttribute("pageSize", size); // Utiliser pageSize pour correspondre au HTML
    model.addAttribute("title", "BooqIn - My Collections");
    model.addAttribute("content", "pages/collections/userCollections");
    
    return "layout";
  }
  


  
  @GetMapping("/{id}")
  public String showCollectionDetails(
          @PathVariable Long id, Model model, Principal principal, @AuthenticationPrincipal User currentUser,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "12") int size
  ) {
    try {
      homeWebController.prepareModel(model, principal, "collection-details");

      BookCollectionShow collection = bookCollectionService.findBookCollectionById(id);
      model.addAttribute("collection", collection);
      model.addAttribute("currentUser", currentUser);

      // Pagination des livres
      int totalBooks = collection.books().size();
      int totalPages = (int) Math.ceil((double) totalBooks / size);
      int start = page * size;
      int end = Math.min(start + size, totalBooks);
      List<BookIndex> paginatedBooks = totalBooks > 0 ? collection.books().subList(start, end) : Collections.emptyList();

      model.addAttribute("paginatedBooks", paginatedBooks);
      model.addAttribute("currentPage", page);
      model.addAttribute("pageSize", size);
      model.addAttribute("totalBooks", totalBooks);
      model.addAttribute("totalPages", totalPages);

      boolean isOwner = (currentUser != null && Objects.equals(collection.user().id(), currentUser.getId()));
      model.addAttribute("isOwner", isOwner);
      if (currentUser != null) {
        boolean isFollowing = followService.isFollowing(currentUser.getId(), id, "COLLECTION");
        model.addAttribute("isFollowing", isFollowing);
        long followersCount = followService.countFollowers(id, "COLLECTION");
        model.addAttribute("followersCount", followersCount);

        try {
          LendingResponseDTO lendingData = bookLendingInterface.findAllUserLendingsAsDTO(currentUser.getId(), LendStatus.ALL);
          StringBuilder logMsg = new StringBuilder();
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
        } catch (Exception e) {
          model.addAttribute("lentBooks", Collections.emptyList());
          model.addAttribute("borrowedBooks", Collections.emptyList());
        }
      }


      model.addAttribute("title", "BooqIn - " + collection.title());
      model.addAttribute("content", "pages/collections/details");
      model.addAttribute("votes", voteService.getVoteCounts(id, "COLLECTION"));
      var totalComments = commentServiceInterface.countByEntity(id, "COLLECTION");
      var comments = commentServiceInterface.getCommentTreeByEntity(id, "COLLECTION");
      if (currentUser != null) {
        model.addAttribute("user", currentUser);
        Map<Long, VoteType> commentVotes = userVoteService.collectCommentVotes(currentUser.getId(), comments);
        model.addAttribute("userCommentVotes", commentVotes);
      }
      model.addAttribute("comments", comments);
      model.addAttribute("commentCount", totalComments);
      return "layout";
    } catch (ResourceNotFoundException e) {
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
    try {
      BookCollectionShow collectionOpt = bookCollectionService.findBookCollectionById(collectionId);
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

    model.addAttribute("bookCollection", new BookCollectionCreate("", "", false, Optional.empty()));

    // Définir le titre et le contenu
    model.addAttribute("title", "BooqIn - Create Collection");
    model.addAttribute("content", "pages/collections/create");

    return "layout";
  }

  @PostMapping("/new")
  public String createCollection(
          @Valid @ModelAttribute("bookCollection") BookCollectionCreate bookCollection,
          Model model,
          Principal principal,
          @AuthenticationPrincipal User currentUser
  ) {
    try {
//      bookCollection.setOwner(currentUser);
//      bookCollection.setPublic(isPublic != null && isPublic);
      BookCollectionShow createdCollection = bookCollectionService.createBookCollection(bookCollection, currentUser);
      return "redirect:/collections";
    } catch (ResourceNotFoundException e) {
      // Préparer le modèle commun même en cas d'erreur
      homeWebController.prepareModel(model, principal, "collection-create");

      model.addAttribute("error", e.getMessage());
      model.addAttribute("title", "BooqIn - Error");
      model.addAttribute("content", "pages/collections/create");

      return "layout";
    }
  }

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
      return "redirect:/collections?error=notFound";
    }
    try {
      BookCollectionShow existingCollection = bookCollectionService.findBookCollectionById(id);
      if (!verifyIsSameUser(currentUser, existingCollection)) {
        return "redirect:/pages/collections?error=notOwner";
      }

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

  @PostMapping("/vote")
  public String vote(@RequestParam("id") Long collectionId, @RequestParam("voteType") VoteType voteType, @AuthenticationPrincipal User currentUser, Model model) {
    if (currentUser == null) {
      return "redirect:/";
    }
    voteService.castVote(new VoteCreate(currentUser.getId(), collectionId, "COLLECTION", voteType));
    // return to the same collection
    return "redirect:/collections/" + collectionId;
  }

  private boolean verifyIsSameUser(User currentUser, BookCollectionShow collection) {
    return Objects.equals(collection.user().id(), currentUser.getId());
  }
  
  @GetMapping("/smart/new")
  public String showCreatSmartCollectionForm(Model model, Principal principal) {
    // Préparer le modèle commun
    homeWebController.prepareModel(model, principal, "collection-create");
    
    model.addAttribute("bookCollection", new BookCollectionCreate("", "", false, Optional.empty()));
    
    // Définir le titre et le contenu
    model.addAttribute("title", "BooqIn - Create Collection");
    model.addAttribute("content", "pages/collections/createSmart");
    
    return "layout";
  }
  
  @PostMapping("/smart/new")
  public String createCollectionSmart(
          @Valid @ModelAttribute("bookCollection") BookCollectionCreate bookCollection,
          Model model,
          Principal principal,
          @AuthenticationPrincipal User currentUser
  ) {
    try {
      BookCollectionShow createdCollection = bookCollectionService.createBookCollection(bookCollection, currentUser);
      return "redirect:/collections";
    } catch (ResourceNotFoundException e) {
      homeWebController.prepareModel(model, principal, "collection-create");
      
      model.addAttribute("error", e.getMessage());
      model.addAttribute("title", "BooqIn - Error");
      model.addAttribute("content", "pages/collections/createSmart");
      
      return "layout";
    }
  }
}