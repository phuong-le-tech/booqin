package fr.uge.booqIn.controller.web;

import org.springframework.context.annotation.Profile;

import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.LendStatus;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@Profile("!demo")
@Controller
@RequestMapping("/loans")
public class BookLendingWebController {
  
  private final UserServiceInterface userService;
  private final BookServiceInterface bookService;
  private final BookLendingInterface bookLendingInterface;
  private final HomeWebController homeWebController;
  
  public BookLendingWebController(
          UserServiceInterface userService,
          BookServiceInterface bookService,
          BookLendingInterface bookLendingInterface, HomeWebController homeWebController) {
    this.userService = userService;
    this.bookService = bookService;
    this.bookLendingInterface = bookLendingInterface;
    this.homeWebController = homeWebController;
  }
  
  @GetMapping
  public String getLendingsPage(
          @RequestParam(required = false, defaultValue = "BORROWED") LendStatus type,
          @AuthenticationPrincipal User currentUser,
          Model model , Principal principal) {
    homeWebController.prepareModel(model, principal, "cart");
      if (!(type == LendStatus.BORROWED) &&
              !(type == LendStatus.LENT) &&
              !(type ==  LendStatus.ALL)) {
      model.addAttribute("errorMessage", "Invalid loan type. Accepted values are: borrowed, lent, all");
      model.addAttribute("title", "Erreur");
      return "layout";
    }
    
    if (currentUser.getId() == null) {
      model.addAttribute("errorMessage", "ID utilisateur requis");
      model.addAttribute("title", "Erreur");
      return "layout";
    }
    
    LendingResponseDTO responseDTO = bookLendingInterface.findAllUserLendingsAsDTO(currentUser.getId(), type);
    
    model.addAttribute("lendingData", responseDTO);
    model.addAttribute("activeTab", type);
    model.addAttribute("userId", currentUser.getId());
    model.addAttribute("title", "Vos Prêts de Livres");
    model.addAttribute("content", "pages/bookLending/bookLending");
    
    return "layout";
  }
  
  @GetMapping("/confirm-loan")
  public String confirmLoan(
          @RequestParam Long lendingId,

          RedirectAttributes redirectAttributes) {
    
    boolean confirmed = bookLendingInterface.confirmLoan(lendingId);
    
    if (confirmed) {
      redirectAttributes.addFlashAttribute("successMessage", "Prêt confirmé avec succès");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Échec de la confirmation du prêt");
    }
    
    return "redirect:/loans?userId=" + LendStatus.ALL;
  }
  
  @GetMapping("/ship-book")
  public String shipBook(
          @RequestParam Long lendingId,
          RedirectAttributes redirectAttributes) {
    
    boolean shipped = bookLendingInterface.markAsShipped(lendingId);
    
    if (shipped) {
      redirectAttributes.addFlashAttribute("successMessage", "Book marked as successfully shipped");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Failed to mark book as shipped");
    }
    
    return "redirect:/loans?type=" + LendStatus.BORROWED;
  }
  
  @GetMapping("/return-book")
  public String returnBook(
          @RequestParam Long lendingId,
          RedirectAttributes redirectAttributes) {
    
    boolean returned = bookLendingInterface.returnBook(lendingId);
    
    if (returned) {
      redirectAttributes.addFlashAttribute("successMessage", "Book marked as successfully returned");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Failed to mark book as returned");
    }
    
    return "redirect:/loans?type=" + LendStatus.BORROWED;
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
          @AuthenticationPrincipal User currentUser,
          RedirectAttributes redirectAttributes) {
    
    boolean added = bookLendingInterface.loanable(bookId, currentUser.getId());
    
    if (added) {
      redirectAttributes.addFlashAttribute("successMessage", "\"Book successfully added from loan list\"");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Failed to add book to lending list");
    }
    
    return "redirect:/loans?type=" + LendStatus.LENT;
  }
  
  
  
  @PostMapping("/remove-loanable")
  public String removeBookFromLoanable(
          @RequestParam Long bookId,
          @AuthenticationPrincipal User currentUser,
          RedirectAttributes redirectAttributes) {
    
    boolean removed = bookLendingInterface.isNoMoreLoanable(bookId, currentUser.getId());
    
    if (removed) {
      redirectAttributes.addFlashAttribute("successMessage", "Book successfully removed from loan list");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Failure to remove book from lending list");
    }
    
    return "redirect:/loans?type=" + LendStatus.LENT;
  }
  
  @GetMapping("/my-lendings")
  public String getCurrentUserLendings(@RequestParam Long userId, Model model) {
    LendingResponseDTO responseDTO = bookLendingInterface.findAllUserLendingsAsDTO(userId, LendStatus.LENT);
    
    model.addAttribute("lendingData", responseDTO);
    model.addAttribute("userId", userId);
    model.addAttribute("title", "My Lending Books");
    model.addAttribute("content", "pages/bookLending/bookLending");
    model.addAttribute("activeTab", "lent");
    
    return "layout";
  }
  
  @GetMapping("/ticket-download/{lendingId}")
  public void downloadTicket(@PathVariable Long lendingId, HttpServletResponse response) throws IOException {
    byte[] ticketPDF = bookLendingInterface.getShippingTicketBytes(lendingId);
    
    if (ticketPDF == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Étiquette non trouvée");
      return;
    }
    
    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=etiquette_expedition_" + lendingId + ".pdf");
    response.setContentLength(ticketPDF.length);
    
    try (ServletOutputStream outputStream = response.getOutputStream()) {
      outputStream.write(ticketPDF);
      outputStream.flush();
    }
  }
}