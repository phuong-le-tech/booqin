package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.BookController;
import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.factory.ApiResponse;
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
    private BookController bookController;
    private Book testBook;

    @BeforeEach
    void setUp() {
        bookService = Mockito.mock(BookServiceInterface.class);
        bookController = new BookController(bookService);
        testBook = createTestBook();
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
                new BookIndex(1L, "Test Book 1", authorNames1, date1),
                new BookIndex(2L, "Test Book 2", authorNames2, date2)
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
                new BookIndex(2L, "Z Book", authorNames2, date2),
                new BookIndex(1L, "A Book", authorNames1, date1)
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
        Date publicationDate = new Date(), createdAt = new Date(), updatedAt = new Date();

        BookShow bookShow = new BookShow(
                bookId,
                isbns,
                "Test Book",
                authorDtos,
                "A test book description",
                publicationDate,
                createdAt,
                updatedAt,
                categories
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
    @DisplayName("POST /books - Should create and return new book")
    void createBook_ShouldReturnCreatedBook() {
        // Given
        Set<String> isbns = new HashSet<>(Collections.singleton("1234567890"));
        Set<Author> authors = new HashSet<>(Collections.singleton(new Author("John", "Doe")));
        Set<String> categories = new HashSet<>(Collections.singleton("Fiction"));

        Book book = new Book.Builder()
                .title("New Book")
                .description("A new test book")
                .authors(authors)
                .isbns(isbns)
                .categories(categories)
                .publicationDate(new Date())
                .build();

        Book createdBook = new Book.Builder()
                .id(1L)
                .title("New Book")
                .description("A new test book")
                .authors(authors)
                .isbns(isbns)
                .categories(categories)
                .publicationDate(new Date())
                .build();

        when(bookService.createBook(any(Book.class))).thenReturn(createdBook);

        // When
        ResponseEntity<ApiResponse<Book>> response = bookController.createBook(book);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponse<Book> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book created successfully", apiResponse.message());
        assertEquals(createdBook, apiResponse.data());
        verify(bookService).createBook(eq(book));
    }

    @Test
    @DisplayName("POST /books - With duplicate ISBN should throw DuplicateResourceException")
    void createBook_WithDuplicateIsbn_ShouldThrowException() {
        // Given
        Set<String> isbns = new HashSet<>(Collections.singleton("1234567890"));
        Set<Author> authors = new HashSet<>(Collections.singleton(new Author("John", "Doe")));
        Book book = new Book.Builder()
                .title("Duplicate Book")
                .description("A book with duplicate ISBN")
                .authors(authors)
                .isbns(isbns)
                .build();
        when(bookService.createBook(any(Book.class)))
                .thenThrow(new DuplicateResourceException("Book", "ISBN", "1234567890"));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
                bookController.createBook(book)
        );
        verify(bookService).createBook(eq(book));
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
        Date publicationDate = new Date(), createdAt = new Date(), updatedAt = new Date();
        BookShow patchedBook = new BookShow(
                bookId,
                isbns,
                "Patched Book",
                authorDtos,
                "A patched description",
                publicationDate,
                createdAt,
                updatedAt,
                categories
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
