package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.StatutPayment;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.CartRepository;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartServiceInterface {

  private final CartRepository cartRepository;
  private final BookRepository bookRepository;
  private final UserServiceInterface userService;

  public CartServiceImpl(CartRepository cartRepository, BookRepository bookRepository, UserServiceInterface userService) {
    this.cartRepository = cartRepository;
    this.bookRepository = bookRepository;
    this.userService = userService;
  }


  @Override
  public Cart getCart(Long cartId) {
    return cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
  }

  @Override
  public Cart updateCartStatus(Long cartId, StatutPayment status) {
    Cart cart = getCart(cartId);
    cart.setStatut(status);
    return cartRepository.save(cart);
  }


  @Override
  @Transactional
  public boolean addBookToCart(Long id, Long bookId) {

    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

    Cart cart = getCart(id);

    List<Book> books = new ArrayList<>(cart.getBooks());
    if (!books.contains(book)) {
      books.add(book);
      cart.setBooks(books);
      cartRepository.save(cart);
      return true;
    }

    return false;
  }

  @Override
  @Transactional
  public boolean removeBookFromCart(Long user, Long bookId) {

    Cart cart = getCart(user);
    Book book = bookRepository.findById(bookId).orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

    if (cart.getBooks().contains(book)) {
      cart.getBooks().remove(book);
      cartRepository.save(cart);
      return true;
    }
    return false;
  }

  @Override
  @Transactional
  public ShopCartDto getCartDto(Long user) {
    Cart cart = getCart(user);
    List<BookIndex> bookIndexes = cart.getBooks().stream()
            .map(book -> new BookIndex(
                    book.getId(),
                    book.getTitle(),
                    book.getAuthors().stream()
                            .map(author -> author.getFirstName() + " " + author.getLastName())
                            .collect(Collectors.toSet()),
                    book.getIsbns(),
                    book.getPublicationDate()
            ))
            .toList();

    return new ShopCartDto(cart.getId(), user, bookIndexes);
  }


  @Override
  @Transactional
  public boolean clearCart(Long user) {
    Cart cart = getCart(user);
    cart.setStatut(StatutPayment.PENDING);
    cart.getBooks().clear();
    cartRepository.save(cart);
    return true;
  }
}
