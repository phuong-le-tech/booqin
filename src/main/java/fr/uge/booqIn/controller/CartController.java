package fr.uge.booqIn.controller;

import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.BookCollectionService;
import fr.uge.booqIn.service.CartService;
import fr.uge.booqIn.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final BookCollectionService bookService;

    public CartController(CartService cartService, UserService userService , BookCollectionService bookService) {
        this.cartService = cartService;
        this.userService = userService;
      this.bookService = bookService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> createCart(@RequestParam Long userId, @RequestBody(required = false) List<Long> bookIds) {
        Optional<User> userOptional = userService.findEntityById(userId);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("⚠️ Utilisateur introuvable avec ID : " + userId);
        }
        
        var user = userOptional.get();
        //Todo a garder
        //List<Book> books = (bookIds == null || bookIds.isEmpty()) ? new ArrayList<>() : bookService.findBooksByIds(bookIds);
        List<Book> books = new ArrayList<>();
        Cart cart = cartService.createCart(user, books);
        System.out.println("Cart created with ID: " + cart);
        return ResponseEntity.ok(cart);
    }


}
