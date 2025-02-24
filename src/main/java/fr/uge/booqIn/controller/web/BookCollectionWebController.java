package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.service.BookCollectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/collections")
public class BookCollectionWebController {
  private final BookCollectionService bookCollectionService;

  public BookCollectionWebController(BookCollectionService bookCollectionService) {
    this.bookCollectionService = bookCollectionService;
  }

  public String showCollections(Model model) {
    return "collections";
  }

  @GetMapping("/public")
  public String showPublicCollections(Model model) {
    List<BookCollection> collections = bookCollectionService.getPublicCollections();
    model.addAttribute("collections", collections);
    return "collections/public";
  }
}
