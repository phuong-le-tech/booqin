package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.enums.StatusPayment;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.CartRepository;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import java.util.ConcurrentModificationException;
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
  @Transactional
  public Cart getCart(Long cartId) {
    var user = userService.findUserByIdToIndex(cartId);
    return cartRepository.findById(user.id())
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public Cart updateCartStatus(Long cartId, StatusPayment status) {
    Cart cart = getCart(cartId);
    cart.setStatut(status);
    return cartRepository.save(cart);
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public boolean addBookToCart(Long userId, Long bookId) {
    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

    Cart cart = getCart(userId);
    System.out.println("Book " + cart.getBooks());

    if (!cart.getBooks().contains(book)) {
      cart.getBooks().add(book);
      System.out.println("Book added to cart." + cartRepository.save(cart));

      return true;
    }

    System.out.println("Book already exists, not adding.");
    System.out.println("Cart: " + cart.getBooks());

    return false;
  }


  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public boolean removeBookFromCart(Long user, Long bookId) {
    cartRepository.deleteBookFromCart(user, bookId);
    return true;
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
                    book.getPublicationDate()
            ))
            .toList();

    return new ShopCartDto(cart.getId(), user, bookIndexes);
  }


  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public boolean clearCart(Long user) {
    Cart cart = getCart(user);
    cart.setStatut(StatusPayment.PENDING);
    cart.getBooks().clear();
    cartRepository.save(cart);
    return true;
  }

  @Recover
  public Cart recoverFromOptimisticLockingFailureOnUpdate(ObjectOptimisticLockingFailureException ex, Long cartId, StatusPayment status) {
    throw new ConcurrentModificationException("The cart has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnAddBook(ObjectOptimisticLockingFailureException ex, Long userId, Long bookId) {
    throw new ConcurrentModificationException("The cart has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnRemoveBook(ObjectOptimisticLockingFailureException ex, Long user, Long bookId) {
    throw new ConcurrentModificationException("The cart has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnClearCart(ObjectOptimisticLockingFailureException ex, Long user) {
    throw new ConcurrentModificationException("The cart has been modified by someone else. Please try again.", ex);
  }
}