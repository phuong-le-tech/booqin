package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.service.BookCollectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.List;

@Controller
public class BookCollectionWebController {
  private final BookCollectionService bookCollectionService;

  public BookCollectionWebController(BookCollectionService bookCollectionService) {
    this.bookCollectionService = bookCollectionService;
  }

  public String showPublicCollections(Model model) {
    List<BookCollection> collections = bookCollectionService.getPublicCollections();
    model.addAttribute("collections", collections);
    return "collections/public";
  }
}
