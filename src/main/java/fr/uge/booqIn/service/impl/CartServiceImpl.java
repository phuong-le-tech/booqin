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
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartServiceInterface {

  private final CartRepository cartRepository;
  private final BookRepository bookRepository;
  private final UserServiceInterface userService;

  public CartServiceImpl(@NonNull CartRepository cartRepository,
                         @NonNull BookRepository bookRepository,
                         @NonNull UserServiceInterface userService) {
    this.cartRepository = cartRepository;
    this.bookRepository = bookRepository;
    this.userService = userService;
  }

  @Override
  @Transactional
  public Cart getCart(@NonNull Long userId) {
    Objects.requireNonNull(userId, "User ID cannot be null");
    var user = userService.findUserById(userId);
    return cartRepository.findById(user.getCart().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", userId));
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public Cart updateCartStatus(@NonNull Long cartId, @NonNull StatusPayment status) {
    Objects.requireNonNull(cartId, "Cart ID cannot be null");
    Objects.requireNonNull(status, "Status cannot be null");

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
  public boolean addBookToCart(@NonNull Long userId, @NonNull Long bookId) {
    Objects.requireNonNull(userId, "User ID cannot be null");
    Objects.requireNonNull(bookId, "Book ID cannot be null");

    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

    Cart cart = getCart(userId);

    if (!cart.getBooks().contains(book)) {
      cart.getBooks().add(book);
      cartRepository.save(cart);
      return true;
    }

    return false;
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public boolean removeBookFromCart(@NonNull Long userId, @NonNull Long bookId) {
    Objects.requireNonNull(userId, "User ID cannot be null");
    Objects.requireNonNull(bookId, "Book ID cannot be null");

    cartRepository.deleteBookFromCart(userId, bookId);
    return true;
  }

  @Override
  @Transactional
  public ShopCartDto getCartDto(@NonNull Long userId) {
    Objects.requireNonNull(userId, "User ID cannot be null");

    Cart cart = getCart(userId);
    List<BookIndex> bookIndexes = cart.getBooks().stream()
            .map(book -> new BookIndex(
                    book.getId(),
                    book.getTitle(),
                    book.getAuthors().stream()
                            .map(author -> author.getFirstName() + " " + author.getLastName())
                            .collect(Collectors.toSet()),
                    book.getPublicationDate(),
                    book.getThumbnailPath()
            ))
            .toList();

    return new ShopCartDto(cart.getId(), userId, bookIndexes);
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public boolean clearCart(@NonNull Long userId) {
    Objects.requireNonNull(userId, "User ID cannot be null");

    Cart cart = getCart(userId);
    cart.setStatut(StatusPayment.PENDING);
    cart.getBooks().clear();
    cartRepository.save(cart);
    return true;
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void clearCartAfterPayment(@NonNull Long cartId) {
    Objects.requireNonNull(cartId, "Cart ID cannot be null");

    Cart cart = getCart(cartId);
    cart.setStatut(StatusPayment.PENDING);
    cart.getBooks().clear();
    cartRepository.save(cart);
  }

  @Recover
  public Cart recoverFromOptimisticLockingFailureOnUpdate(
          ObjectOptimisticLockingFailureException ex,
          Long cartId,
          StatusPayment status) {
    throw new ConcurrentModificationException("The cart has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnAddBook(
          ObjectOptimisticLockingFailureException ex,
          Long userId,
          Long bookId) {
    throw new ConcurrentModificationException("The cart has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnRemoveBook(
          ObjectOptimisticLockingFailureException ex,
          Long userId,
          Long bookId) {
    throw new ConcurrentModificationException("The cart has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnClearCart(
          ObjectOptimisticLockingFailureException ex,
          Long userId) {
    throw new ConcurrentModificationException("The cart has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureOnClearCartAfterPayment(
          ObjectOptimisticLockingFailureException ex,
          Long cartId) {
    throw new ConcurrentModificationException("The cart has been modified by someone else. Please try again.", ex);
  }
}