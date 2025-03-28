package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.dto.book.BookCreate;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.event.BooqInEventPublisher;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.service.impl.BookServiceImpl;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("BookService Tests")
class BookServiceTest {

  private BookRepository bookRepository;
  private AuthorRepository authorRepository;
  private BookMapper bookMapper;
  private BooqInEventPublisher eventPublisher;
  private BookServiceInterface bookService;
  private ImageService imageService;
  private Book testBook;

  @BeforeEach
  void setUp() {
    bookRepository = Mockito.mock(BookRepository.class);
    authorRepository = Mockito.mock(AuthorRepository.class);
    bookMapper = Mockito.mock(BookMapper.class);
    eventPublisher = Mockito.mock(BooqInEventPublisher.class);
    imageService = Mockito.mock(ImageService.class);

    bookService = new BookServiceImpl(bookRepository, authorRepository, bookMapper, eventPublisher, imageService);
    testBook = createTestBook();
  }

  // Helper method to create a test book
  private Book createTestBook() {
    Set<String> isbns = new HashSet<>(Collections.singleton("9780123456789"));
    Set<Author> authors = new HashSet<>();
    authors.add(new Author("Victor", "Hugo"));
    Set<String> categories = new HashSet<>(Arrays.asList("Classic", "Fiction"));

    Book book = new Book();
    book.setId(1L);
    book.setTitle("Les Misérables");
    book.setDescription("A French historical novel by Victor Hugo");
    book.setAuthors(authors);
    book.setIsbns(isbns);
    book.setCategories(categories);
    book.setPublicationDate(new Date());
    book.setCreatedAt(LocalDateTime.now());
    book.setUpdatedAt(LocalDateTime.now());

    return book;
  }

  @Test
  @DisplayName("findBooks - Should return page of books when filters are applied")
  void findBooks_WithFilters_ShouldReturnFilteredBooks() {
    // Given
    String title = "Les";
    String author = "Hugo";
    String isbn = "9780123456789";
    Pageable pageable = Pageable.ofSize(10);

    List<Book> bookList = Collections.singletonList(testBook);
    Page<Book> bookPage = new PageImpl<>(bookList);

    BookIndex bookIndex = new BookIndex(
            testBook.getId(),
            testBook.getTitle(),
            Set.of("Victor Hugo"),
            testBook.getPublicationDate(),
            ""
    );

    when(bookRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(bookPage);
    when(bookMapper.toBookIndex(any(Book.class))).thenReturn(bookIndex);

    // When
    Page<BookIndex> result = bookService.findBooks(title, author, isbn, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(bookIndex, result.getContent().get(0));

    verify(bookRepository).findAll(any(Specification.class), eq(pageable));
    verify(bookMapper).toBookIndex(eq(testBook));
  }

  @Test
  @DisplayName("findById - Should return book when it exists")
  void findById_WithExistingId_ShouldReturnBook() {
    // Given
    Long bookId = testBook.getId();
    BookShow bookShow = new BookShow(
            bookId,
            testBook.getIsbns(),
            testBook.getTitle(),
            Set.of(new AuthorDto(1L, "Victor", "Hugo")),
            testBook.getDescription(),
            testBook.getPublicationDate(),
            testBook.getCreatedAt(),
            testBook.getUpdatedAt(),
            testBook.getCategories(),
            "",
            ""
    );

    when(bookRepository.findByIdWithAuthors(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIdWithIsbns(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIdWithCategories(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookMapper.toBookShow(any(Book.class))).thenReturn(bookShow);

    // When
    BookShow result = bookService.findById(bookId);

    // Then
    assertNotNull(result);
    assertEquals(bookId, result.id());
    assertEquals(testBook.getTitle(), result.title());

    verify(bookRepository).findByIdWithAuthors(eq(bookId));
    verify(bookRepository).findByIdWithIsbns(eq(bookId));
    verify(bookRepository).findByIdWithCategories(eq(bookId));
    verify(bookMapper).toBookShow(any(Book.class));
  }

  @Test
  @DisplayName("findById - Should throw ResourceNotFoundException when book doesn't exist")
  void findById_WithNonExistingId_ShouldThrowException() {
    // Given
    Long bookId = 999L;

    when(bookRepository.findByIdWithAuthors(eq(bookId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () -> bookService.findById(bookId));

    verify(bookRepository).findByIdWithAuthors(eq(bookId));
    verifyNoInteractions(bookMapper);
  }

  @Test
  @DisplayName("createBook - Should create and return new book")
  void createBook_WithValidBook_ShouldCreateAndReturnBook() {
    // Given
    // Mock the authorRepository to handle author lookup and creation
    when(authorRepository.findByFirstNameAndLastName(anyString(), anyString()))
            .thenReturn(Optional.empty());
    when(authorRepository.save(any(Author.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    BookCreate newBook = new BookCreate(
            "New Book",
            new ArrayList<>(Collections.singletonList("John Doe")),
            "A new test book",
            new Date(),
            new ArrayList<>(Collections.singletonList("9890789776"))
    );

    when(bookRepository.findBookByIsbnInSet(anyString())).thenReturn(Optional.empty());
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> {
              Book savedBook = invocation.getArgument(0);
              // Simulate the ID being assigned by the database
              if (savedBook.getId() == null) {
                savedBook.setId(1L);
              }
              return savedBook;
            });

    // When
    Book result = bookService.createFromDto(newBook);

    // Then
    assertNotNull(result);
    assertEquals(newBook.title(), result.getTitle());
    assertEquals(newBook.description(), result.getDescription());
    assertTrue(result.getIsbns().contains("9890789776"));
    assertFalse(result.getAuthors().isEmpty(), "Authors should not be empty");

    verify(bookRepository).save(any(Book.class));
    ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
    verify(bookRepository).save(bookCaptor.capture());
    Book capturedBook = bookCaptor.getValue();
    assertEquals(newBook.title(), capturedBook.getTitle());
    assertNotNull(capturedBook.getCreatedAt());
    assertNotNull(capturedBook.getUpdatedAt());
  }

  @Test
  @DisplayName("createBook - Should handle authors properly")
  void createBook_WithAuthors_ShouldHandleThemProperly() {
    // Given
    when(authorRepository.findByFirstNameAndLastName(anyString(), anyString()))
            .thenReturn(Optional.empty());

    BookCreate newBook = new BookCreate(
            "Mixed Authors Book",
            new ArrayList<>(Arrays.asList("Victor Hugo", "Jane Austen")),
            "A book with mixed authors",
            new Date(),
            new ArrayList<>(Collections.singleton("1234567890"))
    );

    when(bookRepository.findBookByIsbnInSet(anyString())).thenReturn(Optional.empty());

    // When
    Book result = bookService.createFromDto(newBook);

    // Then
    verify(authorRepository, times(2)).findByFirstNameAndLastName(anyString(), anyString());
    verify(authorRepository, times(2)).save(any(Author.class));
    verify(bookRepository).save(any(Book.class));
  }

  @Test
  @DisplayName("createBook - Should throw DuplicateResourceException for duplicate ISBN")
  void createBook_WithDuplicateIsbn_ShouldThrowException() {
    // Given
    String duplicateIsbn = "1234567890";
    Book existingBook = new Book();
    existingBook.setId(2L);
    existingBook.setTitle("Existing Book");

    BookCreate newBook = new BookCreate(
            "New Book",
            new ArrayList<>(Collections.singleton("John Doe")),
            "A new test book",
            new Date(),
            new ArrayList<>(Collections.singleton(duplicateIsbn))
    );

    when(bookRepository.findBookByIsbnInSet(eq(duplicateIsbn))).thenReturn(Optional.of(existingBook));

    // When & Then
    DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
            bookService.createFromDto(newBook)
    );

    verify(bookRepository).findBookByIsbnInSet(eq(duplicateIsbn));
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  @DisplayName("updateBook - Should update and return updated book")
  void updateBook_WithValidBook_ShouldUpdateAndReturnBook() {
    // Given
    Long bookId = 1L;
    Book bookToUpdate = new Book();
    bookToUpdate.setId(bookId);
    bookToUpdate.setTitle("Updated Title");
    bookToUpdate.setDescription("Updated description");
    bookToUpdate.setIsbns(new HashSet<>(Collections.singleton("1234567890")));

    when(bookRepository.existsById(eq(bookId))).thenReturn(true);
    when(bookRepository.findBookByIsbnInSet(anyString())).thenReturn(Optional.empty());
    when(bookRepository.save(any(Book.class))).thenReturn(bookToUpdate);

    // When
    Book result = bookService.updateBook(bookToUpdate);

    // Then
    assertNotNull(result);
    assertEquals(bookToUpdate.getTitle(), result.getTitle());

    verify(bookRepository).existsById(eq(bookId));
    verify(bookRepository).save(any(Book.class));
    ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
    verify(bookRepository).save(bookCaptor.capture());
    Book capturedBook = bookCaptor.getValue();
    assertEquals(bookToUpdate.getTitle(), capturedBook.getTitle());
    assertNotNull(capturedBook.getUpdatedAt());
  }

  @Test
  @DisplayName("updateBook - Should throw ResourceNotFoundException when book doesn't exist")
  void updateBook_WithNonExistingId_ShouldThrowException() {
    // Given
    Long bookId = 999L;
    Book bookToUpdate = new Book();
    bookToUpdate.setId(bookId);
    bookToUpdate.setTitle("Non-existent Book");

    when(bookRepository.existsById(eq(bookId))).thenReturn(false);

    // When & Then
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            bookService.updateBook(bookToUpdate)
    );

    verify(bookRepository).existsById(eq(bookId));
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  @DisplayName("updateBook - Should throw DuplicateResourceException for duplicate ISBN in another book")
  void updateBook_WithDuplicateIsbnInAnotherBook_ShouldThrowException() {
    // Given
    Long bookId = 1L;
    String duplicateIsbn = "1234567890";

    Book anotherBook = new Book();
    anotherBook.setId(2L); // Different ID

    Book bookToUpdate = new Book();
    bookToUpdate.setId(bookId);
    bookToUpdate.setTitle("Book to Update");
    bookToUpdate.setIsbns(new HashSet<>(Collections.singleton(duplicateIsbn)));

    when(bookRepository.existsById(eq(bookId))).thenReturn(true);
    when(bookRepository.findBookByIsbnInSet(eq(duplicateIsbn))).thenReturn(Optional.of(anotherBook));

    // When & Then
    DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
            bookService.updateBook(bookToUpdate)
    );

    verify(bookRepository).existsById(eq(bookId));
    verify(bookRepository).findBookByIsbnInSet(eq(duplicateIsbn));
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  @DisplayName("patchBook - Should patch and return updated book")
  void patchBook_WithValidUpdate_ShouldPatchAndReturnBook() {
    // Given
    Long bookId = 1L;
    String updatedTitle = "Patched Title";
    String updatedDescription = "Patched description";
    Set<String> updatedIsbns = new HashSet<>(Collections.singleton("9876543210"));
    Date updatedDate = new Date();

    BookUpdate bookUpdate = new BookUpdate(
            Optional.of(updatedTitle),
            Optional.of(updatedIsbns),
            Optional.of(updatedDescription),
            Optional.of(updatedDate),
            Optional.empty()
    );

    BookShow expectedBookShow = new BookShow(
            bookId,
            updatedIsbns,
            updatedTitle,
            Set.of(new AuthorDto(1L, "Victor", "Hugo")),
            updatedDescription,
            updatedDate,
            testBook.getCreatedAt(),
            LocalDateTime.now(),
            testBook.getCategories(),
            "",
            ""
    );

    when(bookRepository.findByIdWithAuthors(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIdWithIsbns(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIdWithCategories(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findBookByIsbnInSet(anyString())).thenReturn(Optional.empty());
    when(bookRepository.save(any(Book.class))).thenReturn(testBook);
    when(bookMapper.toBookShow(any(Book.class))).thenReturn(expectedBookShow);

    // When
    BookShow result = bookService.patchBook(bookId, bookUpdate);

    // Then
    assertNotNull(result);
    assertEquals(updatedTitle, result.title());
    assertEquals(updatedDescription, result.description());
    assertEquals(updatedIsbns, result.isbns());
    assertEquals(updatedDate, result.publicationDate());

    verify(bookRepository).findByIdWithAuthors(eq(bookId));
    verify(bookRepository).findByIdWithIsbns(eq(bookId));
    verify(bookRepository).findByIdWithCategories(eq(bookId));
    verify(bookRepository).save(any(Book.class));
    verify(bookMapper).toBookShow(any(Book.class));
  }

  @Test
  @DisplayName("patchBook - With non-existing ID should throw ResourceNotFoundException")
  void patchBook_WithNonExistingId_ShouldThrowException() {
    // Given
    Long bookId = 999L;
    BookUpdate bookUpdate = new BookUpdate(
            Optional.of("New Title"),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    when(bookRepository.findByIdWithAuthors(eq(bookId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            bookService.patchBook(bookId, bookUpdate)
    );

    verify(bookRepository).findByIdWithAuthors(eq(bookId));
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  @DisplayName("patchBook - With duplicate ISBN should throw DuplicateResourceException")
  void patchBook_WithDuplicateIsbn_ShouldThrowException() {
    // Given
    Long bookId = 1L;
    String duplicateIsbn = "1234567890";
    Set<String> updatedIsbns = new HashSet<>(Collections.singleton(duplicateIsbn));

    Book anotherBook = new Book();
    anotherBook.setId(2L); // Different ID

    BookUpdate bookUpdate = new BookUpdate(
            Optional.empty(),
            Optional.of(updatedIsbns),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    when(bookRepository.findByIdWithAuthors(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIdWithIsbns(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIdWithCategories(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findBookByIsbnInSet(eq(duplicateIsbn))).thenReturn(Optional.of(anotherBook));

    // When & Then
    assertThrows(DuplicateResourceException.class, () ->
            bookService.patchBook(bookId, bookUpdate)
    );

    verify(bookRepository).findByIdWithAuthors(eq(bookId));
    verify(bookRepository).findByIdWithIsbns(eq(bookId));
    verify(bookRepository).findByIdWithCategories(eq(bookId));
    verify(bookRepository).findBookByIsbnInSet(eq(duplicateIsbn));
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  @DisplayName("deleteBook - Should delete existing book")
  void deleteBook_WithValidId_ShouldDeleteBook() {
    // Given
    Long bookId = 1L;

    when(bookRepository.findByIdWithAuthors(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIdWithIsbns(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIdWithCategories(eq(bookId))).thenReturn(Optional.of(testBook));
    doNothing().when(bookRepository).delete(any(Book.class));

    // When
    bookService.deleteBook(bookId);

    // Then
    verify(bookRepository).findByIdWithAuthors(eq(bookId));
    verify(bookRepository).findByIdWithIsbns(eq(bookId));
    verify(bookRepository).findByIdWithCategories(eq(bookId));
    verify(bookRepository).delete(eq(testBook));
  }

  @Test
  @DisplayName("deleteBook - With non-existing ID should throw ResourceNotFoundException")
  void deleteBook_WithNonExistingId_ShouldThrowException() {
    // Given
    Long bookId = 999L;

    when(bookRepository.findByIdWithAuthors(eq(bookId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            bookService.deleteBook(bookId)
    );

    verify(bookRepository).findByIdWithAuthors(eq(bookId));
    verify(bookRepository, never()).delete(any(Book.class));
  }

  @Test
  @DisplayName("saveFromApi - Should update existing book when ISBN exists")
  void saveFromApi_WithExistingIsbn_ShouldUpdateBook() {
    // Given
    String existingIsbn = "1234567890";
    String newIsbn = "9876543210";
    String newCategory = "SciFi";

    Book existingBook = new Book();
    existingBook.setId(1L);
    existingBook.setTitle("Existing Book");
    existingBook.setIsbns(new HashSet<>(Collections.singleton(existingIsbn)));
    existingBook.setCategories(new HashSet<>(Collections.singleton("Fiction")));
    existingBook.setAuthors(new HashSet<>());

    Book apiBook = new Book();
    apiBook.setTitle("API Book");
    apiBook.setDescription("Updated description");
    apiBook.setIsbns(new HashSet<>(Arrays.asList(existingIsbn, newIsbn)));
    apiBook.setCategories(new HashSet<>(Collections.singleton(newCategory)));

    when(bookRepository.findBookByIsbnInSet(eq(existingIsbn))).thenReturn(Optional.of(existingBook));
    when(bookRepository.findBookByIsbnInSet(eq(newIsbn))).thenReturn(Optional.empty());
    when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

    // When
    bookService.saveFromApi(apiBook);

    // Then
    verify(bookRepository).findBookByIsbnInSet(eq(existingIsbn));
    verify(bookRepository).save(any(Book.class));

    ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
    verify(bookRepository).save(bookCaptor.capture());
    Book capturedBook = bookCaptor.getValue();
    assertEquals(existingBook.getId(), capturedBook.getId());
    assertEquals("Updated description", capturedBook.getDescription());
  }

  @Test
  @DisplayName("saveFromApi - With null book should throw NullPointerException")
  void saveFromApi_WithNullBook_ShouldThrowException() {
    // When & Then
    assertThrows(NullPointerException.class, () ->
            bookService.saveFromApi(null)
    );

    verifyNoInteractions(bookRepository);
  }

  @Test
  @DisplayName("findBookById - Should return book when it exists")
  void findBookById_WithExistingId_ShouldReturnBook() {
    // Given
    Long bookId = testBook.getId();

    when(bookRepository.findByIdWithAuthors(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIdWithIsbns(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIdWithCategories(eq(bookId))).thenReturn(Optional.of(testBook));

    // When
    Book result = bookService.findBookById(bookId);

    // Then
    assertNotNull(result);
    assertEquals(bookId, result.getId());
    assertEquals(testBook.getTitle(), result.getTitle());

    verify(bookRepository).findByIdWithAuthors(eq(bookId));
    verify(bookRepository).findByIdWithIsbns(eq(bookId));
    verify(bookRepository).findByIdWithCategories(eq(bookId));
  }

  @Test
  @DisplayName("findBookById - Should throw ResourceNotFoundException when book doesn't exist")
  void findBookById_WithNonExistingId_ShouldThrowException() {
    // Given
    Long bookId = 999L;

    when(bookRepository.findByIdWithAuthors(eq(bookId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            bookService.findBookById(bookId)
    );

    verify(bookRepository).findByIdWithAuthors(eq(bookId));
  }
}