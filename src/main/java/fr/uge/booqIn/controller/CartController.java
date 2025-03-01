package fr.uge.booqIn.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/controller/api/cart")
public class CartController {
/*

    private final CartService cartService;
    private final UserService userService;
    private final BookCollectionService bookService;

    public CartController(CartService cartService, UserService userService , BookCollectionService bookService) {
        this.cartService = cartService;
        this.userService = userService;
      this.bookService = bookService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createCart(@RequestParam Long userId, @RequestBody(required = false) List<Long> bookIds) {
        Optional<User> userOptional = userService.findEntityById(userId);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }
        
        var user = userOptional.get();
        //Todo a garder
        //List<Book> books = (bookIds == null || bookIds.isEmpty()) ? new ArrayList<>() : bookService.findBooksByIds(bookIds);
        List<Book> books = new ArrayList<>();
        Cart cart = cartService.createCart(user, books);
        System.out.println("Cart created with ID: " + cart);
        return ResponseEntity.ok(ApiResponse.success("Cart created", cart));
    }
*/


}
