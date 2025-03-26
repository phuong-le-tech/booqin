package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.impl.CartServiceImpl;
import fr.uge.booqIn.service.impl.UserServiceImpl;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartWebController {

  private final CartServiceImpl cartService;
  private final UserServiceImpl userService;
  private final HomeWebController homeWebController;
  private final BookLendingInterface bookLendingInterface;

  @Autowired
  public CartWebController(CartServiceImpl cartService, UserServiceImpl userService, HomeWebController homeWebController, BookLendingInterface bookLendingInterface) {
    this.cartService = cartService;
    this.userService = userService;
    this.homeWebController = homeWebController;
    this.bookLendingInterface = bookLendingInterface;
  }

  @GetMapping()
  public String viewCart(@AuthenticationPrincipal User currentUser, Model model, Principal principal) {
    // Préparer le modèle commun
    homeWebController.prepareModel(model, principal, "cart");

    if (currentUser.getId() == null) {
      model.addAttribute("error", "Utilisateur non spécifié. Veuillez vous connecter.");
      model.addAttribute("title", "BooqIn - Erreur");
      model.addAttribute("content", "pages/error");
      return "layout"; // Utiliser le layout avec la page d'erreur
    }

    Optional<ShopCartDto> cartOptional = Optional.ofNullable(cartService.getCartDto(currentUser.getId() ));

    if (cartOptional.isEmpty()) {
      model.addAttribute("cart", null);
      model.addAttribute("subtotal", 0.0);
      model.addAttribute("total", 5.99);
    } else {
      ShopCartDto cart = cartOptional.get();
      //double subtotal = cart.books().stream().mapToDouble(book -> book.price()).sum();
      double subtotal = 0.99;
      double total = subtotal + 5.99; // Frais de livraison

      model.addAttribute("cart", cart);
      model.addAttribute("subtotal", subtotal);
      model.addAttribute("total", total);
    }

    // Définir le titre et le contenu
    model.addAttribute("title", "BooqIn - Votre Panier");
    model.addAttribute("content", "pages/shoppingCart/shoppingCart");

    return "layout";
  }

  @PostMapping("/remove-book")
  public String removeBookFromCart(@RequestParam Long userId, @RequestParam Long bookId, RedirectAttributes redirectAttributes) {
    var canceled = bookLendingInterface.cancelBook(bookId, userId);
    boolean updated = cartService.removeBookFromCart(userId, bookId);

    if (!updated) {
      redirectAttributes.addFlashAttribute("error", "Impossible de supprimer le livre du panier.");
    }

    return "redirect:/cart?userId=" + userId;
  }

  @PostMapping("/clear-cart")
  public String clearCart(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
    var listBookLending = bookLendingInterface.findUserLendings(userId);
    bookLendingInterface.ListClearBookstoLend(listBookLending);
    boolean cleared = cartService.clearCart(userId);

    if (!cleared) {
      redirectAttributes.addFlashAttribute("error", "Impossible de vider le panier.");
    } else {
      redirectAttributes.addFlashAttribute("success", "Votre panier a été vidé avec succès.");
    }

    return "redirect:/cart?userId=" + userId;
  }

  @PostMapping("/add-book")
  public String addBookToCart(@RequestParam Long userId, @RequestParam Long bookId, RedirectAttributes redirectAttributes) {
    
    boolean addedToLending = bookLendingInterface.reserveBook(bookId, userId);
    
    if (!addedToLending) {
      redirectAttributes.addFlashAttribute("error", "Impossible de réserver le livre. vous êtes sur liste d'attente.");
      return "redirect:/cart?userId=" + userId;
    }
    
    boolean added = cartService.addBookToCart(userId, bookId);
    if (!added) {
      redirectAttributes.addFlashAttribute("error", "Le livre est déjà dans le panier.");
    } else {
      redirectAttributes.addFlashAttribute("success", "Livre ajouté au panier avec succès.");
    }
    return "redirect:/cart?userId=" + userId;
  }
}