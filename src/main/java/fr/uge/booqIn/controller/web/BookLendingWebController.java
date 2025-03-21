package fr.uge.booqIn.controller.web;


import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/loans")
public class BookLendingWebController {
  
  private final UserServiceInterface userService;
  private final BookServiceInterface bookService;
  private final BookLendingInterface bookLendingInterface;
  
  public BookLendingWebController(
          UserServiceInterface userService,
          BookServiceInterface bookService,
          BookLendingInterface bookLendingInterface) {
    this.userService = userService;
    this.bookService = bookService;
    this.bookLendingInterface = bookLendingInterface;
  }
  
  @GetMapping
  public String getLendingsPage(
          @RequestParam(required = false, defaultValue = "borrowed") String type,
          @AuthenticationPrincipal User currentUser,
          Model model) {
    // Validation des types de prêts
    if (!type.equalsIgnoreCase("borrowed") &&
            !type.equalsIgnoreCase("lent") &&
            !type.equalsIgnoreCase("all")) {
      model.addAttribute("errorMessage", "Type de prêt invalide. Les valeurs acceptées sont: borrowed, lent, all");
      model.addAttribute("title", "Erreur");
      return "layout";
    }
    
    if (currentUser.getId() == null) {
      System.out.println("ID utilisateur requis");
      model.addAttribute("errorMessage", "ID utilisateur requis");
      model.addAttribute("title", "Erreur");
      return "layout";
    }
    
    LendingResponseDTO responseDTO = bookLendingInterface.findAllUserLendingsAsDTO(currentUser.getId(), type);
    
    model.addAttribute("lendingData", responseDTO);
    model.addAttribute("activeTab", type);
    model.addAttribute("userId", currentUser.getId());
    model.addAttribute("title", "Vos Prêts de Livres");
    model.addAttribute("content", "pages/bookLending/index");
    
    return "layout";
  }
  
  @GetMapping("/confirm-loan")
  public String confirmLoan(
          @RequestParam Long lendingId,
          @RequestParam Long userId,
          RedirectAttributes redirectAttributes) {
    
    boolean confirmed = bookLendingInterface.confirmLoan(lendingId);
    
    if (confirmed) {
      redirectAttributes.addFlashAttribute("successMessage", "Prêt confirmé avec succès");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Échec de la confirmation du prêt");
    }
    
    return "redirect:/loans?userId=" + userId;
  }
  
  @GetMapping("/ship-book")
  public String shipBook(
          @RequestParam Long lendingId,
          @RequestParam Long userId,
          RedirectAttributes redirectAttributes) {
    
    boolean shipped = bookLendingInterface.markAsShipped(lendingId);
    
    if (shipped) {
      redirectAttributes.addFlashAttribute("successMessage", "Livre marqué comme expédié avec succès");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Échec du marquage du livre comme expédié");
    }
    
    return "redirect:/loans?userId=" + userId;
  }
  
  @GetMapping("/return-book")
  public String returnBook(
          @RequestParam Long lendingId,
          @RequestParam Long userId,
          RedirectAttributes redirectAttributes) {
    
    boolean returned = bookLendingInterface.returnBook(lendingId);
    
    if (returned) {
      redirectAttributes.addFlashAttribute("successMessage", "Livre marqué comme retourné avec succès");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Échec du marquage du livre comme retourné");
    }
    
    return "redirect:/loans?userId=" + userId;
  }
  
  @GetMapping("/make-loanable")
  public String makeBookLoanableForm(@RequestParam(required = false) Long bookId, Model model) {
    model.addAttribute("bookId", bookId);
    model.addAttribute("title", "Ajouter un livre à prêter");
    model.addAttribute("content", "pages/bookLending/make-loanable");
    return "layout";
  }
  
  @PostMapping("/make-loanable")
  public String makeBookLoanable(
          @RequestParam Long bookId,
          @RequestParam Long userId,
          RedirectAttributes redirectAttributes) {
    
    boolean added = bookLendingInterface.loanable(bookId, userId);
    
    if (added) {
      redirectAttributes.addFlashAttribute("successMessage", "Livre ajouté à la liste de prêt avec succès");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Échec de l'ajout du livre à la liste de prêt");
    }
    
    return "redirect:/loans?userId=" + userId + "&type=lent";
  }
  
  @PostMapping("/remove-loanable")
  public String removeBookFromLoanable(
          @RequestParam Long bookId,
          @RequestParam Long userId,
          RedirectAttributes redirectAttributes) {
    
    boolean removed = bookLendingInterface.isNoMoreLoanable(bookId, userId);
    
    if (removed) {
      redirectAttributes.addFlashAttribute("successMessage", "Livre retiré de la liste de prêt avec succès");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Échec du retrait du livre de la liste de prêt");
    }
    
    return "redirect:/loans?userId=" + userId + "&type=lent";
  }
  
  @GetMapping("/my-lendings")
  public String getCurrentUserLendings(@RequestParam Long userId, Model model) {
    LendingResponseDTO responseDTO = bookLendingInterface.findAllUserLendingsAsDTO(userId, "lent");
    
    model.addAttribute("lendingData", responseDTO);
    model.addAttribute("userId", userId);
    model.addAttribute("title", "Mes Livres à Prêter");
    model.addAttribute("content", "pages/bookLending/index");
    model.addAttribute("activeTab", "lent");
    
    return "layout";
  }
}