package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.BookService;
import fr.uge.booqIn.service.CartService;
import fr.uge.booqIn.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

public class CartWebController {
  
  private final CartService cartService;
  private final UserService userService;
  private final BookService bookService;
  
  
public CartWebController(CartService cartService, UserService userService , BookService bookService) {
    this.cartService = cartService;
    this.userService = userService;
    this.bookService = bookService;
  }
  
  @GetMapping("/{userId}")
  public String showCart(@PathVariable Long userId, Model model) {
    Optional<User> userOptional = userService.findEntityById(userId);
    if (userOptional.isEmpty()) {
      return "error";
    }
    
    Cart cart = cartService.getCart(userOptional.get());
    model.addAttribute("cart", cart);
    return "cart";
  }
  
  @PostMapping("/add-book")
  public String addBookToCart(@RequestParam Long userId, @RequestParam Long bookId) {
    Optional<User> userOptional = userService.findEntityById(userId);
    Optional<Book> bookOptional = bookService.findBookById(bookId);
    
    if (userOptional.isEmpty() || bookOptional.isEmpty()) {
      return "redirect:/cart/" + userId;
    }
    
    cartService.addBookToCart(userOptional.get(), bookOptional.get());
    return "redirect:/cart/" + userId;
  }
  
  @PostMapping("/remove-book")
  public String removeBookFromCart(@RequestParam Long userId, @RequestParam Long bookId) {
    Optional<User> userOptional = userService.findEntityById(userId);
    Optional<Book> bookOptional = bookService.findBookById(bookId);
    
    if (userOptional.isEmpty() || bookOptional.isEmpty()) {
      return "redirect:/cart/" + userId;
    }
    
    cartService.removeBookFromCart(userOptional.get(), bookOptional.get());
    return "redirect:/cart/" + userId;
  }
  
  @PostMapping("/clear/{userId}")
  public String clearCart(@PathVariable Long userId) {
    Optional<User> userOptional = userService.findEntityById(userId);
    
    if (userOptional.isEmpty()) {
      return "redirect:/cart/" + userId;
    }
    
    cartService.clearCart(userOptional.get());
    return "redirect:/cart/" + userId;
  }
}
