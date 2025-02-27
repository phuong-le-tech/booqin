package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.BookCollectionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/collections")
public class BookCollectionWebController {
  private final BookCollectionService bookCollectionService;

  public BookCollectionWebController(BookCollectionService bookCollectionService) {
    this.bookCollectionService = bookCollectionService;
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
}
