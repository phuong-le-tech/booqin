package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.service.impl.BookServiceImpl;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWebController {

  private static final Logger logger = LoggerFactory.getLogger(AdminWebController.class);

  private final UserServiceInterface userService;
  private final BookServiceInterface bookService;
  private final BookCollectionServiceInterface bookCollectionService;
  private final HomeWebController homeWebController;

  public AdminWebController(UserServiceInterface userService, BookServiceInterface bookService, BookCollectionServiceInterface bookCollectionService, HomeWebController homeWebController) {
    this.userService = userService;
    this.bookService = bookService;
    this.bookCollectionService = bookCollectionService;
    this.homeWebController = homeWebController;
  }

  @GetMapping("")
  public String adminDashboard(
          Model model,
          Principal principal,
          @RequestParam(value = "tab", defaultValue = "users") String activeTab,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection,
          @RequestParam(required = false) String firstName,
          @RequestParam(required = false) String lastName,
          @RequestParam(required = false) String email,
          @RequestParam(required = false) String title,
          @RequestParam(required = false) String author,
          @RequestParam(required = false) String isbn,
          @RequestParam(required = false) String description,
          @RequestParam(required = false, defaultValue = "false") boolean isPublic,
          @RequestParam(required = false) String search
  ) {
    homeWebController.prepareModel(model, principal, "admin");
    model.addAttribute("activeTab", activeTab);

    Sort sort = sortDirection.equalsIgnoreCase("desc") ?
            Sort.by(sortField).descending() :
            Sort.by(sortField).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    switch (activeTab) {
      case "users":
        if (search != null && !search.isEmpty()) {
          firstName = search;
          lastName = search;
          email = search;
        }
        Page<UserIndex> users = userService.findUsers(firstName, lastName, email, pageable);
        model.addAttribute("users", users);
        break;

      case "books":
        if (search != null && !search.isEmpty()) {
          title = search;
          author = search;
          isbn = search;
        }
        Page<BookIndex> books = bookService.findBooks(title, author, isbn, pageable);
        model.addAttribute("books", books);
        break;

      case "collections":
        if (search != null && !search.isEmpty()) {
          title = search;
          description = search;
        }
        Page<BookCollectionIndex> collections = bookCollectionService.findBookCollections(title, description, isPublic, pageable);
        model.addAttribute("collections", collections);
        break;
    }

    model.addAttribute("title", "BooqIn - Administration");
    model.addAttribute("content", "pages/admin/admin-panel");
    return "layout";
  }

  @GetMapping("/users/new")
  public String showAddUserForm(Model model, Principal principal) {
    homeWebController.prepareModel(model, principal, "admin");

    User user = new User();
    model.addAttribute("user", user);

    model.addAttribute("title", "BooqIn - Add a user");
    model.addAttribute("content", "pages/admin/add-user");
    return "layout";
  }

  @PostMapping("/users/new")
  public String processAddUserForm(
          @ModelAttribute User user,
          @RequestParam(required = false) UserRole role,
          Model model,
          Principal principal,
          RedirectAttributes redirectAttributes)
  {
    try {
      User createdUser = userService.createUser(user);

      redirectAttributes.addFlashAttribute("success", "User added with success");
      return "redirect:/admin?tab=users";

    } catch (Exception e) {
      homeWebController.prepareModel(model, principal, "admin");

      model.addAttribute("error", e.getMessage());
      model.addAttribute("user", user);

      model.addAttribute("title", "BooqIn - Add a user");
      model.addAttribute("content", "pages/admin/add-user");
      return "layout";
    }
  }

  @GetMapping("/users/{id}/edit")
  public String showEditUserForm(@PathVariable Long id, Model model, Principal principal) {
    homeWebController.prepareModel(model, principal, "admin");

    try {
      UserShow userShow = userService.findById(id);
      model.addAttribute("user", userShow);

      UserRole role = userShow.role();
      model.addAttribute("role", role.getRole());

      model.addAttribute("title", "BooqIn - Modifier l'utilisateur");
      model.addAttribute("content", "pages/admin/edit-user");

      return "layout";

    } catch (Exception e) {
      return "redirect:/admin?tab=users";
    }
  }

  @PostMapping("/users/{id}/edit")
  public String processEditUserForm(
          @PathVariable Long id,
          @ModelAttribute User user,
          @RequestParam(required = false) UserRole role,
          Model model,
          Principal principal,
          RedirectAttributes redirectAttributes)
  {
    try {
      User updatedUser = userService.updateUser(user);
      redirectAttributes.addFlashAttribute("success", "User removed with success");
      return "redirect:/admin?tab=users";
    } catch (Exception e) {
      homeWebController.prepareModel(model, principal, "admin");

      model.addAttribute("error", e.getMessage());
      model.addAttribute("user", user);
      model.addAttribute("userRoles", role.getRole());

      model.addAttribute("title", "BooqIn - Edit the user");
      model.addAttribute("content", "pages/admin/edit-user");
      return "layout";
    }
  }

  @PostMapping("/users/{id}/delete")
  public String deleteUser(
          @PathVariable Long id,
          RedirectAttributes redirectAttributes)
  {
    try {
      userService.deleteUser(id);
      redirectAttributes.addFlashAttribute("success", "User removed with success");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error while removing: " + e.getMessage());
    }
    return "redirect:/admin?tab=users";
  }
}