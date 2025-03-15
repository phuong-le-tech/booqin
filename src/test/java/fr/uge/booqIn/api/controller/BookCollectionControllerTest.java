package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.BookCollectionController;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.*;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
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
@DisplayName("BookCollectionController Tests")
class BookCollectionControllerTest {

    private BookCollectionServiceInterface bookCollectionService;
    private BookCollectionController bookCollectionController;
    private BookCollectionMapper bookCollectionMapper;
    @BeforeEach
    void setUp() {
        bookCollectionService = Mockito.mock(BookCollectionServiceInterface.class);
        bookCollectionController = new BookCollectionController(bookCollectionService);
        bookCollectionMapper = new BookCollectionMapper();
    }

    private Address createTestAddress() {
        return new Address("123", "Main St", "New York", "10001");
    }

    private User createTestUser() {
        Address address = createTestAddress();
        return new User("John", "Doe", "john.doe@example.com", "Password123", address, UserRole.USER);
    }

    @Test
    @DisplayName("GET /collections - Returns a paged list of collections")
    void getAllBookCollections_ShouldReturnPageOfCollections() {
        // Given
        int page = 0;
        int size = 10;
        String sortField = "id";
        String sortDirection = "asc";
        String title = "Test";
        String description = "Desc";
        Long userId = 1L;

        // Create a dummy BookCollectionIndex object (using a mock or real dummy instance)
        BookCollectionIndex dummyIndex = Mockito.mock(BookCollectionIndex.class);
        Page<BookCollectionIndex> pageCollections = new PageImpl<>(List.of(dummyIndex));
        when(bookCollectionService.findBookCollections(eq(title), eq(description), eq(userId), any(PageRequest.class)))
                .thenReturn(pageCollections);

        // When
        ResponseEntity<ApiResponse<Page<BookCollectionIndex>>> response =
                bookCollectionController.getAllBookCollections(page, size, sortField, sortDirection, title, description, userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Page<BookCollectionIndex>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Collections found", apiResponse.message());
        assertEquals(pageCollections, apiResponse.data());

        // Verify that the pageable was built correctly
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(bookCollectionService).findBookCollections(eq(title), eq(description), eq(userId), pageRequestCaptor.capture());
        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(page, capturedPageRequest.getPageNumber());
        assertEquals(size, capturedPageRequest.getPageSize());
        assertEquals(Sort.by(sortField).ascending(), capturedPageRequest.getSort());
    }

    @Test
    @DisplayName("POST /collections - Creates a new collection")
    void createCollection_ShouldReturnCreatedCollection() {
        // Given
        // Create an input BookCollection with a dummy owner (the owner will be set by the controller)
        User dummyUser = createTestUser();
        BookCollection inputCollection = new BookCollection("Test Collection", "Test Description", dummyUser);
        BookCollection createdCollection = new BookCollection("Test Collection", "Test Description", dummyUser);
        createdCollection.setId(1L);

        when(bookCollectionService.createBookCollection(inputCollection)).thenReturn(createdCollection);

        // When
        ResponseEntity<ApiResponse<BookCollection>> response =
                bookCollectionController.createCollection(inputCollection, dummyUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponse<BookCollection> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Collection created successfully", apiResponse.message());
        assertEquals(createdCollection, apiResponse.data());
        verify(bookCollectionService).createBookCollection(inputCollection);
    }

    @Test
    @DisplayName("GET /collections/{id} - Returns a collection")
    void getCollection_ShouldReturnCollection() {
        // Given
        Long collectionId = 1L;
        BookCollectionShow collectionShow = Mockito.mock(BookCollectionShow.class);
        when(bookCollectionService.findById(collectionId)).thenReturn(collectionShow);

        // When
        ResponseEntity<ApiResponse<BookCollectionShow>> response =
                bookCollectionController.getCollection(collectionId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<BookCollectionShow> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Collection found", apiResponse.message());
        assertEquals(collectionShow, apiResponse.data());
        verify(bookCollectionService).findById(collectionId);
    }

    @Test
    @DisplayName("DELETE /collections/{id} - Deletes a collection")
    void deleteCollection_ShouldReturnSuccessResponse() {
        // Given
        Long collectionId = 1L;
        doNothing().when(bookCollectionService).deleteBookCollection(collectionId);
        User dummyUser = createTestUser();

        // When
        ResponseEntity<ApiResponse<Void>> response =
                bookCollectionController.deleteCollection(collectionId, dummyUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Void> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Collection deleted successfully", apiResponse.message());
        assertNull(apiResponse.data());
        verify(bookCollectionService).deleteBookCollection(collectionId);
    }

    @Test
    @DisplayName("PUT /collections - Updates a collection")
    void updateCollection_ShouldReturnUpdatedCollection() {
        // Given
        User dummyUser = createTestUser();
        BookCollection inputCollection = new BookCollection("Test Collection", "Test Description", dummyUser);
        inputCollection.setId(1L);
        BookCollection updatedCollection = new BookCollection("Updated Collection", "Updated Description", dummyUser);
        updatedCollection.setId(1L);

        when(bookCollectionService.updateBookCollection(inputCollection)).thenReturn(updatedCollection);

        // When
        ResponseEntity<ApiResponse<BookCollection>> response =
                bookCollectionController.updateCollection(inputCollection);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<BookCollection> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("BookCollection updated successfully", apiResponse.message());
        assertEquals(updatedCollection, apiResponse.data());
        verify(bookCollectionService).updateBookCollection(inputCollection);
    }

    @Test
    @DisplayName("PATCH /collections/{id} - Partially updates a collection")
    void patchCollection_ShouldReturnUpdatedCollectionShow() {
        // Given
        Long collectionId = 1L;
        // Assuming BookCollectionUpdate has methods name() and description() returning Optionals
        BookCollectionUpdate update = new BookCollectionUpdate(Optional.of("Updated Title"),
                                                               Optional.of("Updated Description"),
                                                               Optional.empty());
        BookCollectionShow updatedShow = Mockito.mock(BookCollectionShow.class);
        when(bookCollectionService.patchBookCollection(collectionId, update)).thenReturn(updatedShow);
        User dummyUser = createTestUser();

        // When
        ResponseEntity<ApiResponse<BookCollectionShow>> response =
                bookCollectionController.patchCollection(collectionId, dummyUser, update);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<BookCollectionShow> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Collection updated successfully", apiResponse.message());
        assertEquals(updatedShow, apiResponse.data());
        verify(bookCollectionService).patchBookCollection(collectionId, update);
    }

    @Test
    @DisplayName("POST /collections/{collectionId}/books/{bookId} - Adds a book to a collection")
    void addBookToCollection_ShouldReturnUpdatedCollection() {
        // Given
        Long collectionId = 1L;
        Long bookId = 1L;
        BookCollection updatedCollection = new BookCollection("Test Collection", "Test Description", createTestUser());
        updatedCollection.setId(collectionId);
        when(bookCollectionService.addBookToCollection(collectionId, bookId)).thenReturn(updatedCollection);

        // When
        ResponseEntity<ApiResponse<BookCollection>> response =
                bookCollectionController.addBookToCollection(collectionId, bookId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<BookCollection> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book added to collection successfully", apiResponse.message());
        assertEquals(updatedCollection, apiResponse.data());
        verify(bookCollectionService).addBookToCollection(collectionId, bookId);
    }

    @Test
    @DisplayName("DELETE /collections/{collectionId}/books/{bookId} - Removes a book from a collection")
    void removeBookFromCollection_ShouldReturnUpdatedCollection() {
        // Given
        Long collectionId = 1L;
        Long bookId = 1L;
        BookCollection updatedCollection = new BookCollection("Test Collection", "Test Description", createTestUser());
        updatedCollection.setId(collectionId);
        when(bookCollectionService.removeBookFromCollection(eq(collectionId), eq(bookId), any(User.class)))
                .thenReturn(updatedCollection);
        User dummyUser = createTestUser();

        // When
        ResponseEntity<ApiResponse<BookCollection>> response =
                bookCollectionController.removeBookFromCollection(collectionId, bookId, dummyUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<BookCollection> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book removed from collection successfully", apiResponse.message());
        assertEquals(updatedCollection, apiResponse.data());
        verify(bookCollectionService).removeBookFromCollection(collectionId, bookId, dummyUser);
    }

    @Test
    @DisplayName("GET /collections/public - Returns public collections")
    void getPublicCollections_ShouldReturnPublicCollections() {
        // Given
        BookCollectionShow publicCollection = Mockito.mock(BookCollectionShow.class);
        UserShow dummyUserShow = Mockito.mock(UserShow.class);
        when(dummyUserShow.booksToLend()).thenReturn(Set.of());
        when(publicCollection.user()).thenReturn(dummyUserShow);

        // Use a LinkedList so that getFirst() works if needed by the controller implementation
        List<BookCollectionShow> publicCollections = new LinkedList<>();
        publicCollections.add(publicCollection);
        when(bookCollectionService.getPublicCollections()).thenReturn(publicCollections);

        // When
        ResponseEntity<ApiResponse<List<BookCollectionShow>>> response =
                bookCollectionController.getPublicCollections();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<List<BookCollectionShow>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Public collections fetched", apiResponse.message());
        assertEquals(publicCollections, apiResponse.data());
        verify(bookCollectionService).getPublicCollections();
    }


    @Test
    @DisplayName("GET /collections/user - Returns user collections")
    void getUserCollections_ShouldReturnUserCollections() {
        // Given
        BookCollection userCollection = new BookCollection("User Collection", "Description", createTestUser());
        List<BookCollection> userCollections = List.of(userCollection);
        when(bookCollectionService.getUserCollections(any(User.class))).thenReturn(userCollections);
        User dummyUser = createTestUser();

        // When
        ResponseEntity<ApiResponse<List<BookCollection>>> response =
                bookCollectionController.getUserCollections(dummyUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<List<BookCollection>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("User collections fetched", apiResponse.message());
        assertEquals(userCollections, apiResponse.data());
        verify(bookCollectionService).getUserCollections(dummyUser);
    }

    @Test
    @DisplayName("GET /collections/user/with-books - Returns user collections with books")
    void getUserCollectionsWithBooks_ShouldReturnUserCollectionsWithBooks() {
        // Given
        User dummyUser = createTestUser();
        BookCollection userCollection = new BookCollection("User Collection", "Description", dummyUser);
        List<BookCollection> userCollections = List.of(userCollection);
        when(bookCollectionService.getUserCollectionsWithBooks(any(User.class))).thenReturn(userCollections.stream().map( c -> bookCollectionMapper.toBookCollectionShow(c)).toList());


        // When
        ResponseEntity<ApiResponse<List<BookCollection>>> response =
                bookCollectionController.getUserCollectionsWithBooks(dummyUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<List<BookCollection>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("User collections with books fetched", apiResponse.message());
        assertEquals(userCollections, apiResponse.data());
        verify(bookCollectionService).getUserCollectionsWithBooks(dummyUser);
    }

    @Test
    @DisplayName("GET /collections/{collectionId}/books/{bookId} - Returns a book from a collection")
    void getBookFromCollection_ShouldReturnBook() {
        // Given
        Long collectionId = 1L;
        Long bookId = 1L;
        // Create a dummy Book using its Builder
        Book book = new Book.Builder().id(bookId).title("Test Book").build();
        when(bookCollectionService.getBookFromCollection(collectionId, bookId)).thenReturn(book);

        // When
        ResponseEntity<ApiResponse<Book>> response =
                bookCollectionController.getBookFromCollection(collectionId, bookId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Book> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book fetched", apiResponse.message());
        assertEquals(book, apiResponse.data());
        verify(bookCollectionService).getBookFromCollection(collectionId, bookId);
    }
}
