package fr.uge.booqIn.controller.web;



import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.service.impl.CartServiceImpl;
import fr.uge.booqIn.service.impl.UserServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartWebController {
  
  private final CartServiceImpl cartService;
  private final UserServiceImpl userService;
  
  public CartWebController(CartServiceImpl cartService, UserServiceImpl userService) {
    this.cartService = cartService;
    this.userService = userService;
  }
  
  @GetMapping()
  public String viewCart(@RequestParam(required = false) Long userId, Model model) {
    if (userId == null) {
      model.addAttribute("error", "Utilisateur non spécifié. Veuillez vous connecter.");
      return "error"; // Redirige vers une page d'erreur (ou gérer autrement)
    }
    
    Optional<ShopCartDto> cartOptional = Optional.ofNullable(cartService.getCartDto(userId));
    
    if (cartOptional.isEmpty()) {
      model.addAttribute("cart", null);
      model.addAttribute("subtotal", 0.0);
      model.addAttribute("total", 5.99);
    } else {
      ShopCartDto cart = cartOptional.get();
      double total = 5.99;
      
      model.addAttribute("cart", cart);
      model.addAttribute("total", total);
    }
    return "shoppingCart/shoppingCart";
  }
    
    @PostMapping("/remove-book")
  public String removeBookFromCart(@RequestParam Long userId, @RequestParam Long bookId, RedirectAttributes redirectAttributes) {
    boolean updated = cartService.removeBookFromCart(userId, bookId);
    
    if (!updated) {
      redirectAttributes.addFlashAttribute("error", "Impossible de supprimer le livre du panier.");
    }
    
    return "redirect:/cart/?userId=" + userId;
  }
  
  @PostMapping("/clear-cart")
  public String clearCart(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
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
    boolean added = cartService.addBookToCart(userId, bookId);
    
    if (!added) {
      redirectAttributes.addFlashAttribute("error", "Le livre est déjà dans le panier.");
    } else {
      redirectAttributes.addFlashAttribute("success", "Livre ajouté au panier avec succès.");
    }
    return "redirect:/cart?userId=" + userId;
  }
}