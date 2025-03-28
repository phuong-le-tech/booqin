package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.BookCollectionController;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.bookCollection.*;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.interfaces.BookCollection;
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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
  private User testUser;

  @BeforeEach
  void setUp() {
    bookCollectionService = Mockito.mock(BookCollectionServiceInterface.class);
    bookCollectionController = new BookCollectionController(bookCollectionService);
    testUser = createTestUser();
  }

  private User createTestUser() {
    User user = new User("John", "Doe", "john.doe@example.com", "password", null, UserRole.USER);
    user.setId(1L);
    return user;
  }

  @Test
  @DisplayName("GET /collections - Should return paged collections with ascending sort")
  void getAllBookCollections_ShouldReturnPageOfCollections() {
    // Given
    int page = 0, size = 10;
    String sortField = "id", sortDirection = "asc";
    String title = "Test Collection", description = "Test Description";
    boolean isPublic = true;

    List<BookCollectionIndex> collectionList = List.of(
            new BookCollectionIndex(1L, "Collection 1", "Description 1", "John Doe", 1L, 2, true, null),
            new BookCollectionIndex(2L, "Collection 2", "Description 2", "John Doe", 1L, 3, true, null)
    );
    Page<BookCollectionIndex> collectionPage = new PageImpl<>(collectionList);

    when(bookCollectionService.findBookCollections(eq(title), eq(description), eq(isPublic), any(PageRequest.class)))
            .thenReturn(collectionPage);

    // When
    ResponseEntity<ApiResponse<Page<BookCollectionIndex>>> response = bookCollectionController.getAllBookCollections(
            page, size, sortField, sortDirection, title, description, isPublic);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Page<BookCollectionIndex>> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Collections found", apiResponse.message());
    assertEquals(collectionPage, apiResponse.data());

    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(bookCollectionService).findBookCollections(eq(title), eq(description), eq(isPublic), pageRequestCaptor.capture());
    PageRequest capturedPageRequest = pageRequestCaptor.getValue();
    assertEquals(page, capturedPageRequest.getPageNumber());
    assertEquals(size, capturedPageRequest.getPageSize());
    assertEquals(Sort.by(sortField).ascending(), capturedPageRequest.getSort());
  }

  @Test
  @DisplayName("POST /collections - Should create and return a new collection")
  void createCollection_ShouldReturnCreatedCollection() {
    // Given
    BookCollectionCreate collectionCreateDto = new BookCollectionCreate("New Collection", "A new collection description", true, Optional.empty());

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john.doe@example.com", null, UserRole.USER);

    BookCollectionShow createdCollection = new BookCollectionShow(
            1L,
            "New Collection",
            "A new collection description",
            true,
            List.of(),
            userIndex,
            BookCollection.CollectionType.NORMAL,
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    when(bookCollectionService.createBookCollection(any(BookCollectionCreate.class), any(User.class)))
            .thenReturn(createdCollection);

    // When
    ResponseEntity<ApiResponse<BookCollectionShow>> response = bookCollectionController.createCollection(collectionCreateDto, testUser);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    ApiResponse<BookCollectionShow> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Collection created successfully", apiResponse.message());
    assertEquals(createdCollection, apiResponse.data());
    verify(bookCollectionService).createBookCollection(eq(collectionCreateDto), eq(testUser));
  }

  @Test
  @DisplayName("GET /collections/{id} - Should return collection when found")
  void getCollection_ShouldReturnCollection() {
    // Given
    Long collectionId = 1L;
    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john.doe@example.com", null, UserRole.USER);

    BookCollectionShow collection = new BookCollectionShow(
            collectionId,
            "Test Collection",
            "Test Description",
            true,
            List.of(),
            userIndex,
            BookCollection.CollectionType.NORMAL,
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(collection);

    // When
    ResponseEntity<ApiResponse<BookCollectionShow>> response = bookCollectionController.getCollection(collectionId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<BookCollectionShow> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Collection found", apiResponse.message());
    assertEquals(collection, apiResponse.data());
    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
  }

  @Test
  @DisplayName("DELETE /collections/{id} - Should delete collection and return success")
  void deleteCollection_ShouldReturnSuccess() {
    // Given
    Long collectionId = 1L;
    doNothing().when(bookCollectionService).deleteBookCollection(eq(collectionId));

    // When
    ResponseEntity<ApiResponse<Void>> response = bookCollectionController.deleteCollection(collectionId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Void> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Collection deleted successfully", apiResponse.message());
    verify(bookCollectionService).deleteBookCollection(eq(collectionId));
  }

  @Test
  @DisplayName("PATCH /collections/{id} - Should update and return updated collection")
  void updateBookCollection_ShouldReturnUpdatedCollection() {
    // Given
    Long collectionId = 1L;
    BookCollectionUpdate updateDto = new BookCollectionUpdate(Optional.of("Updated Collection"), Optional.of("Updated Description"), false);

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john.doe@example.com", null, UserRole.USER);

    BookCollectionShow updatedCollection = new BookCollectionShow(
            collectionId,
            "Updated Collection",
            "Updated Description",
            false,
            List.of(),
            userIndex,
            BookCollection.CollectionType.NORMAL,
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    when(bookCollectionService.updateBookCollection(eq(collectionId), any(BookCollectionUpdate.class)))
            .thenReturn(updatedCollection);

    // When
    ResponseEntity<ApiResponse<BookCollectionShow>> response = bookCollectionController.updateBookCollection(collectionId, updateDto);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<BookCollectionShow> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Collection updated successfully", apiResponse.message());
    assertEquals(updatedCollection, apiResponse.data());
    verify(bookCollectionService).updateBookCollection(eq(collectionId), eq(updateDto));
  }

  @Test
  @DisplayName("POST /collections/{collectionId}/books/{bookId} - Should add book to collection and return success")
  void addBookToCollection_ShouldReturnSuccess() {
    // Given
    Long collectionId = 1L;
    Long bookId = 2L;
    doNothing().when(bookCollectionService).addBookToCollection(eq(collectionId), eq(bookId));

    // When
    ResponseEntity<ApiResponse<BookCollectionShow>> response = bookCollectionController.addBookToCollection(collectionId, bookId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<BookCollectionShow> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Book added to collection successfully", apiResponse.message());
    verify(bookCollectionService).addBookToCollection(eq(collectionId), eq(bookId));
  }

  @Test
  @DisplayName("DELETE /collections/{collectionId}/books/{bookId} - Should remove book from collection and return success")
  void removeBookFromCollection_ShouldReturnSuccess() {
    // Given
    Long collectionId = 1L;
    Long bookId = 2L;
    doNothing().when(bookCollectionService).removeBookFromCollection(eq(collectionId), eq(bookId));

    // When
    ResponseEntity<ApiResponse<BookCollectionShow>> response = bookCollectionController.removeBookFromCollection(collectionId, bookId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<BookCollectionShow> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Book removed from collection successfully", apiResponse.message());
    verify(bookCollectionService).removeBookFromCollection(eq(collectionId), eq(bookId));
  }

  @Test
  @DisplayName("GET /collections/public - Should return paged public collections")
  void getPublicCollections_ShouldReturnPageOfPublicCollections() {
    // Given
    int page = 0, size = 10;
    String sortField = "id", sortDirection = "asc";

    List<BookCollectionIndex> collectionList = List.of(
            new BookCollectionIndex(1L, "Public Collection 1", "Description 1", "John Doe", 1L, 2, true, null),
            new BookCollectionIndex(2L, "Public Collection 2", "Description 2", "Jane Smith", 2L, 1, true, null)
    );
    Page<BookCollectionIndex> collectionPage = new PageImpl<>(collectionList);

    when(bookCollectionService.getPublicCollections(any(PageRequest.class))).thenReturn(collectionPage);

    // When
    ResponseEntity<ApiResponse<Page<BookCollectionIndex>>> response = bookCollectionController.getPublicCollections(
            page, size, sortField, sortDirection);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Page<BookCollectionIndex>> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Public collections fetched", apiResponse.message());
    assertEquals(collectionPage, apiResponse.data());

    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(bookCollectionService).getPublicCollections(pageRequestCaptor.capture());
    PageRequest capturedPageRequest = pageRequestCaptor.getValue();
    assertEquals(page, capturedPageRequest.getPageNumber());
    assertEquals(size, capturedPageRequest.getPageSize());
    assertEquals(Sort.by(sortField).ascending(), capturedPageRequest.getSort());
  }

  @Test
  @DisplayName("GET /collections/user/public - Should return paged public collections for current user")
  void getPublicUserCollection_ShouldReturnPageOfUserPublicCollections() {
    // Given
    int page = 0, size = 10;
    String sortField = "id", sortDirection = "asc";

    List<BookCollectionIndex> collectionList = List.of(
            new BookCollectionIndex(1L, "User Public Collection 1", "Description 1", "John Doe", 1L, 3, true, null),
            new BookCollectionIndex(2L, "User Public Collection 2", "Description 2", "John Doe", 1L, 2, true, null)
    );
    Page<BookCollectionIndex> collectionPage = new PageImpl<>(collectionList);

    when(bookCollectionService.getPublicUserCollection(eq(testUser), any(PageRequest.class))).thenReturn(collectionPage);

    // When
    ResponseEntity<ApiResponse<Page<BookCollectionIndex>>> response = bookCollectionController.getPublicUserCollection(
            page, size, sortField, sortDirection, testUser);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Page<BookCollectionIndex>> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Public user collection", apiResponse.message());
    assertEquals(collectionPage, apiResponse.data());

    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(bookCollectionService).getPublicUserCollection(eq(testUser), pageRequestCaptor.capture());
    PageRequest capturedPageRequest = pageRequestCaptor.getValue();
    assertEquals(page, capturedPageRequest.getPageNumber());
    assertEquals(size, capturedPageRequest.getPageSize());
    assertEquals(Sort.by(sortField).ascending(), capturedPageRequest.getSort());
  }

  @Test
  @DisplayName("GET /collections/user/private - Should return paged private collections for current user")
  void getPrivateUserCollection_ShouldReturnPageOfUserPrivateCollections() {
    // Given
    int page = 0, size = 10;
    String sortField = "id", sortDirection = "asc";

    List<BookCollectionIndex> collectionList = List.of(
            new BookCollectionIndex(3L, "User Private Collection 1", "Description 1", "John Doe", 1L, 2, false, null),
            new BookCollectionIndex(4L, "User Private Collection 2", "Description 2", "John Doe", 1L, 3, false, null)
    );
    Page<BookCollectionIndex> collectionPage = new PageImpl<>(collectionList);

    when(bookCollectionService.getPrivateUserCollection(eq(testUser), any(PageRequest.class))).thenReturn(collectionPage);

    // When
    ResponseEntity<ApiResponse<Page<BookCollectionIndex>>> response = bookCollectionController.getPrivateUserCollection(
            page, size, sortField, sortDirection, testUser);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Page<BookCollectionIndex>> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Private user collection", apiResponse.message());
    assertEquals(collectionPage, apiResponse.data());

    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(bookCollectionService).getPrivateUserCollection(eq(testUser), pageRequestCaptor.capture());
    PageRequest capturedPageRequest = pageRequestCaptor.getValue();
    assertEquals(page, capturedPageRequest.getPageNumber());
    assertEquals(size, capturedPageRequest.getPageSize());
    assertEquals(Sort.by(sortField).ascending(), capturedPageRequest.getSort());
  }

  @Test
  @DisplayName("GET /collections/user/books - Should return user collections with books")
  void getUserCollectionsWithBooks_ShouldReturnUserCollectionsWithBooks() {
    // Given
    int page = 0, size = 10;
    String sortField = "id", sortDirection = "asc";

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john.doe@example.com", null, UserRole.USER);

    List<BookIndex> booksCollection1 = List.of(
            new BookIndex(1L, "Book 1", Set.of(), new Date(), ""),
            new BookIndex(2L, "Book 2", Set.of(), new Date(), "")
    );

    List<BookIndex> booksCollection2 = List.of(
            new BookIndex(3L, "Book 3", Set.of(), new Date(), "")
    );

    List<BookCollectionShow> collectionList = List.of(
            new BookCollectionShow(1L, "Collection With Books 1", "Description 1", true,
                    booksCollection1, userIndex,             BookCollection.CollectionType.NORMAL, LocalDateTime.now(), LocalDateTime.now()),
            new BookCollectionShow(2L, "Collection With Books 2", "Description 2", false,
                    booksCollection2, userIndex,             BookCollection.CollectionType.NORMAL, LocalDateTime.now(), LocalDateTime.now())
    );
    Page<BookCollectionShow> collectionPage = new PageImpl<>(collectionList);

    when(bookCollectionService.getUserCollectionsWithBooks(eq(testUser), any(PageRequest.class))).thenReturn(collectionPage);

    // When
    ResponseEntity<ApiResponse<Page<BookCollectionShow>>> response = bookCollectionController.getUserCollectionsWithBooks(
            page, size, sortField, sortDirection, testUser);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Page<BookCollectionShow>> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("User collections with books fetched", apiResponse.message());
    assertEquals(collectionPage, apiResponse.data());

    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(bookCollectionService).getUserCollectionsWithBooks(eq(testUser), pageRequestCaptor.capture());
    PageRequest capturedPageRequest = pageRequestCaptor.getValue();
    assertEquals(page, capturedPageRequest.getPageNumber());
    assertEquals(size, capturedPageRequest.getPageSize());
    assertEquals(Sort.by(sortField).ascending(), capturedPageRequest.getSort());
  }
}