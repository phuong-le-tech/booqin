package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.BookController;
import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.dto.book.BookCreate;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.service.ImageService;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("BookController Tests")
class BookControllerTest {

    private BookServiceInterface bookService;
    private ImageService imageService;
    private BookController bookController;
    private Book testBook;
    private BookMapper bookMapper;

    @BeforeEach
    void setUp() {
        bookService = Mockito.mock(BookServiceInterface.class);
        bookController = new BookController(bookService, imageService);
        testBook = createTestBook();
        bookMapper = new BookMapper();
    }

    // Helper method for common test book creation
    private Book createTestBook() {
        Set<String> isbns = new HashSet<>(Collections.singleton("9780123456789"));
        Set<Author> authors = new HashSet<>(Collections.singleton(new Author("Victor", "Hugo")));
        Set<String> categories = new HashSet<>(Arrays.asList("Classic", "Fiction"));
        return new Book.Builder()
                .id(1L)
                .title("Les Misérables")
                .description("A French historical novel by Victor Hugo")
                .authors(authors)
                .isbns(isbns)
                .categories(categories)
                .publicationDate(new Date())
                .build();
    }

    @Test
    @DisplayName("GET /books - Should return paged books with ascending sort")
    void getBooks_ShouldReturnPageOfBooks() {
        // Given
        int page = 0, size = 10;
        String sortField = "title", sortDirection = "asc";
        String title = "Test", author = "John", isbn = "1234567890";

        Set<String> authorNames1 = new HashSet<>(Collections.singleton("John Doe"));
        Set<String> authorNames2 = new HashSet<>(Collections.singleton("Jane Smith"));
        Date date1 = new Date(), date2 = new Date();

        List<BookIndex> bookList = List.of(
                new BookIndex(1L, "Test Book 1", authorNames1, date1, "pathImageTest"),
                new BookIndex(2L, "Test Book 2", authorNames2, date2, "pathImageTest")
        );
        Page<BookIndex> bookPage = new PageImpl<>(bookList);
        when(bookService.findBooks(eq(title), eq(author), eq(isbn), any(PageRequest.class)))
                .thenReturn(bookPage);

        // When
        ResponseEntity<ApiResponse<Page<BookIndex>>> response = bookController.getBooks(
                page, size, sortField, sortDirection, title, author, isbn);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Page<BookIndex>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Books found", apiResponse.message());
        assertEquals(bookPage, apiResponse.data());

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(bookService).findBooks(eq(title), eq(author), eq(isbn), pageRequestCaptor.capture());
        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(page, capturedPageRequest.getPageNumber());
        assertEquals(size, capturedPageRequest.getPageSize());
        assertEquals(Sort.by(sortField).ascending(), capturedPageRequest.getSort());
    }

    @Test
    @DisplayName("GET /books - With descending sort should return paged books")
    void getBooks_WithDescendingSort_ShouldReturnPageOfBooks() {
        // Given
        int page = 0, size = 10;
        String sortField = "title", sortDirection = "desc";
        String title = null, author = null, isbn = null;

        Set<String> authorNames1 = new HashSet<>(Collections.singleton("John Doe"));
        Set<String> authorNames2 = new HashSet<>(Collections.singleton("Jane Smith"));
        Date date1 = new Date(), date2 = new Date();

        List<BookIndex> bookList = List.of(
                new BookIndex(2L, "Z Book", authorNames2, date2, "pathImageTest"),
                new BookIndex(1L, "A Book", authorNames1, date1, "pathImageTest")
        );
        Page<BookIndex> bookPage = new PageImpl<>(bookList);
        when(bookService.findBooks(eq(title), eq(author), eq(isbn), any(PageRequest.class)))
                .thenReturn(bookPage);

        // When
        ResponseEntity<ApiResponse<Page<BookIndex>>> response = bookController.getBooks(
                page, size, sortField, sortDirection, title, author, isbn);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Page<BookIndex>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(bookService).findBooks(eq(title), eq(author), eq(isbn), pageRequestCaptor.capture());
        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(Sort.by(sortField).descending(), capturedPageRequest.getSort());
    }

    @Test
    @DisplayName("GET /books/{id} - Should return a book when found")
    void getBookById_ShouldReturnBook() {
        // Given
        Long bookId = testBook.getId();
        Set<String> categories = new HashSet<>(Collections.singleton("Fiction"));
        Set<String> isbns = new HashSet<>(Collections.singleton("1234567890"));
        Set<AuthorDto> authorDtos = new HashSet<>(Collections.singleton(new AuthorDto(1L, "John", "Doe")));
        Date publicationDate = new Date();
        var createdAt = LocalDateTime.now();
        var updatedAt = LocalDateTime.now();

        BookShow bookShow = new BookShow(
                bookId,
                isbns,
                "Test Book",
                authorDtos,
                "A test book description",
                publicationDate,
                createdAt,
                updatedAt,
                categories,
                "pathImageTest1",
                "pathImageTest2"
        );
        when(bookService.findById(eq(bookId))).thenReturn(bookShow);

        // When
        ResponseEntity<ApiResponse<BookShow>> response = bookController.getBookById(bookId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<BookShow> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book found", apiResponse.message());
        assertEquals(bookShow, apiResponse.data());
        verify(bookService).findById(eq(bookId));
    }

    @Test
    @DisplayName("GET /books/{id} - When book not found, should throw ResourceNotFoundException")
    void getBookById_WhenBookNotFound_ShouldThrowException() {
        // Given
        Long bookId = testBook.getId();
        when(bookService.findById(eq(bookId)))
                .thenThrow(new ResourceNotFoundException("Book", "id", bookId));

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                bookController.getBookById(bookId)
        );
        verify(bookService).findById(eq(bookId));
    }

    @Test
    @DisplayName("POST /books - Should create and return new book as BookShow DTO")
    void createBook_ShouldReturnCreatedBook() {
        // Given
        List<String> isbns = List.of("1234567890");
        List<String> authorNames = List.of("John Doe");
        Date publicationDate = new Date();

        BookCreate bookCreateDto = new BookCreate(
                "New Book",
                authorNames,
                "A new test book",
                publicationDate,
                isbns
        );

        Author authorEntity = new Author("John", "Doe");
        authorEntity.setId(42L); // simulate existing author in DB

        Book createdBook = new Book.Builder()
                .id(1L)
                .title("New Book")
                .description("A new test book")
                .authors(Set.of(authorEntity))
                .isbns(Set.copyOf(isbns))
                .categories(Set.of("Fiction"))
                .publicationDate(publicationDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .coverImagePath("https://example.com/covers/1.jpg")
                .thumbnailPath("https://example.com/thumbnails/1.jpg")
                .build();

        AuthorDto authorDto = new AuthorDto(42L, "John", "Doe");

        BookShow bookShowDto = new BookShow(
                createdBook.getId(),
                createdBook.getIsbns(),
                createdBook.getTitle(),
                Set.of(authorDto),
                createdBook.getDescription(),
                createdBook.getPublicationDate(),
                createdBook.getCreatedAt(),
                createdBook.getUpdatedAt(),
                createdBook.getCategories(),
                createdBook.getCoverImagePath(),
                createdBook.getThumbnailPath()
        );

        when(bookService.createFromDto(any(BookCreate.class))).thenReturn(createdBook);
        when(bookMapper.toBookShow(createdBook)).thenReturn(bookShowDto);

        // When
        ResponseEntity<ApiResponse<BookShow>> response = bookController.create(bookCreateDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ApiResponse<BookShow> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.success());
        assertEquals("Book created successfully", body.message());
        assertEquals(bookShowDto, body.data());

        verify(bookService).createFromDto(any(BookCreate.class));
        verify(bookMapper).toBookShow(createdBook);
    }


    @Test
    @DisplayName("POST /books - With duplicate ISBN should throw DuplicateResourceException")
    void createBook_WithDuplicateIsbn_ShouldThrowException() {
        // Given
        List<String> isbns = new ArrayList<>(Collections.singletonList("1234567890"));
        List<String> authorNames = new ArrayList<>(Collections.singletonList("John Doe"));

        BookCreate bookCreateDto = new BookCreate(
                "Duplicate Book",
                authorNames,
                "A book with duplicate ISBN",
                new Date(),
                isbns
        );

        when(bookService.createFromDto(any(BookCreate.class)))
                .thenThrow(new DuplicateResourceException("Book", "ISBN", "1234567890"));

        // When & Then
        assertThrows(DuplicateResourceException.class, () ->
                bookController.create(bookCreateDto)
        );
        verify(bookService).createFromDto(any(BookCreate.class));
    }

    @Test
    @DisplayName("PUT /books/{id} - Should update and return updated book")
    void updateBook_ShouldReturnUpdatedBook() {
        // Given
        Long bookId = 1L;
        Set<String> isbns = new HashSet<>(Collections.singleton("1234567890"));
        Set<Author> authors = new HashSet<>(Collections.singleton(new Author("John", "Doe")));
        Set<String> categories = new HashSet<>(Collections.singleton("Fiction"));

        Book book = new Book.Builder()
                .title("Updated Book")
                .description("An updated test book")
                .authors(authors)
                .isbns(isbns)
                .categories(categories)
                .publicationDate(new Date())
                .build();

        Book updatedBook = new Book.Builder()
                .id(bookId)
                .title("Updated Book")
                .description("An updated test book")
                .authors(authors)
                .isbns(isbns)
                .categories(categories)
                .publicationDate(new Date())
                .build();

        when(bookService.updateBook(any(Book.class))).thenReturn(updatedBook);

        // When
        ResponseEntity<ApiResponse<Book>> response = bookController.updateBook(bookId, book);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Book> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book updated successfully", apiResponse.message());
        assertEquals(updatedBook, apiResponse.data());

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookService).updateBook(bookCaptor.capture());
        Book capturedBook = bookCaptor.getValue();
        assertEquals(bookId, capturedBook.getId());
    }

    @Test
    @DisplayName("PUT /books/{id} - When book not found, should throw ResourceNotFoundException")
    void updateBook_WithBookNotFound_ShouldThrowException() {
        // Given
        Long bookId = 999L;
        Book book = new Book.Builder()
                .title("Non-existent Book")
                .description("A book that doesn't exist")
                .build();
        when(bookService.updateBook(any(Book.class)))
                .thenThrow(new ResourceNotFoundException("Book", "id", bookId));

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                bookController.updateBook(bookId, book)
        );
        verify(bookService).updateBook(any(Book.class));
    }

    @Test
    @DisplayName("PATCH /books/{id} - Should partially update and return updated book")
    void patchBook_ShouldReturnUpdatedBook() {
        // Given
        Long bookId = 1L;
        Set<String> isbns = new HashSet<>(Collections.singleton("1234567890"));
        Set<AuthorDto> authorDtos = new HashSet<>(Collections.singleton(new AuthorDto(1L, "John", "Doe")));
        Set<String> categories = new HashSet<>(Collections.singleton("Fiction"));
        List<AbstractBookCollection> collections = new ArrayList<>();

        BookUpdate bookUpdate = new BookUpdate(
                Optional.of("Patched Book"),
                Optional.of(isbns),
                Optional.of("A patched description"),
                Optional.of(new Date()),
                Optional.of(collections)
        );
        Date publicationDate = new Date();
        var createdAt = LocalDateTime.now();
        var updatedAt = LocalDateTime.now();

        BookShow patchedBook = new BookShow(
                bookId,
                isbns,
                "Patched Book",
                authorDtos,
                "A patched description",
                publicationDate,
                createdAt,
                updatedAt,
                categories,
                "pathImageTest1",
                "pathImageTest2"
        );
        when(bookService.patchBook(eq(bookId), any(BookUpdate.class))).thenReturn(patchedBook);

        // When
        ResponseEntity<ApiResponse<BookShow>> response = bookController.patchBook(bookId, bookUpdate);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<BookShow> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book updated successfully", apiResponse.message());
        assertEquals(patchedBook, apiResponse.data());
        verify(bookService).patchBook(eq(bookId), eq(bookUpdate));
    }

    @Test
    @DisplayName("DELETE /books/{id} - Should delete book and return success")
    void deleteBook_ShouldReturnSuccess() {
        // Given
        Long bookId = 1L;
        doNothing().when(bookService).deleteBook(eq(bookId));

        // When
        ResponseEntity<ApiResponse<Void>> response = bookController.deleteBook(bookId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Void> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book deleted successfully", apiResponse.message());
        assertNull(apiResponse.data());
        verify(bookService).deleteBook(eq(bookId));
    }

    @Test
    @DisplayName("DELETE /books/{id} - When book not found, should throw ResourceNotFoundException")
    void deleteBook_WithBookNotFound_ShouldThrowException() {
        // Given
        Long bookId = 999L;
        doThrow(new ResourceNotFoundException("Book", "id", bookId))
                .when(bookService).deleteBook(eq(bookId));

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                bookController.deleteBook(bookId)
        );

        verify(bookService).deleteBook(eq(bookId));
    }
}
