package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.bookCollection.BookCollectionCreate;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.entity.*;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.interfaces.BookCollection;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.service.impl.BookCollectionServiceImpl;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("BookCollectionService Tests")
class BookCollectionServiceTest {

  private BookCollectionRepository bookCollectionRepository;
  private BookRepository bookRepository;
  private BookCollectionMapper bookCollectionMapper;
  private BookCollectionServiceInterface bookCollectionService;
  private User testUser;
  private AbstractBookCollection testCollection;
  private Book testBook;

  @BeforeEach
  void setUp() {
    bookCollectionRepository = Mockito.mock(BookCollectionRepository.class);
    bookRepository = Mockito.mock(BookRepository.class);
    bookCollectionMapper = Mockito.mock(BookCollectionMapper.class);

    bookCollectionService = new BookCollectionServiceImpl(bookCollectionRepository, bookRepository, bookCollectionMapper);

    // Setup test data
    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");

    testCollection = new NormalBookCollection.Builder()
            .title("Test Collection")
            .description("A test collection")
            .owner(testUser)
            .isPublic(true)
            .build();
    testCollection.setId(1L);

    testBook = new Book();
    testBook.setId(1L);
    testBook.setTitle("Test Book");
  }

  @Test
  @DisplayName("findBookCollections - Should return page of collections when filters are applied")
  void findBookCollections_WithFilters_ShouldReturnFilteredCollections() {
    // Given
    String name = "Test";
    String description = "test";
    boolean isPublic = true;
    Pageable pageable = Pageable.ofSize(10);

    List<AbstractBookCollection> collectionList = Collections.singletonList(testCollection);
    Page<AbstractBookCollection> collectionPage = new PageImpl<>(collectionList);

    BookCollectionIndex collectionIndex = new BookCollectionIndex(
            testCollection.getId(),
            testCollection.getTitle(),
            testCollection.getDescription(),
            testUser.getFirstName() + " " + testUser.getLastName(),
            testUser.getId(),
            0,
            testCollection.isPublic(),
            BookCollection.CollectionType.NORMAL
    );

    when(bookCollectionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(collectionPage);
    when(bookCollectionMapper.toBookCollectionIndex(any(AbstractBookCollection.class))).thenReturn(collectionIndex);

    // When
    Page<BookCollectionIndex> result = bookCollectionService.findBookCollections(name, description, isPublic, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(collectionIndex, result.getContent().get(0));

    verify(bookCollectionRepository).findAll(any(Specification.class), eq(pageable));
    verify(bookCollectionMapper).toBookCollectionIndex(eq(testCollection));
  }

  @Test
  @DisplayName("findBookCollectionById - Should return collection when it exists")
  void findBookCollectionById_WithExistingId_ShouldReturnCollection() {
    // Given
    Long collectionId = testCollection.getId();
    BookCollectionShow collectionShow = new BookCollectionShow(
            collectionId,
            testCollection.getTitle(),
            testCollection.getDescription(),
            testCollection.isPublic(),
            Collections.emptyList(),
            null,
            BookCollection.CollectionType.NORMAL,
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    when(bookCollectionRepository.findById(eq(collectionId))).thenReturn(Optional.of(testCollection));
    when(bookCollectionMapper.toBookCollectionShow(any(AbstractBookCollection.class))).thenReturn(collectionShow);

    // When
    BookCollectionShow result = bookCollectionService.findBookCollectionById(collectionId);

    // Then
    assertNotNull(result);
    assertEquals(collectionId, result.id());
    assertEquals(testCollection.getTitle(), result.title());

    verify(bookCollectionRepository).findById(eq(collectionId));
    verify(bookCollectionMapper).toBookCollectionShow(eq(testCollection));
  }

  @Test
  @DisplayName("findBookCollectionById - Should throw ResourceNotFoundException when collection doesn't exist")
  void findBookCollectionById_WithNonExistingId_ShouldThrowException() {
    // Given
    Long collectionId = 999L;

    when(bookCollectionRepository.findById(eq(collectionId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            bookCollectionService.findBookCollectionById(collectionId)
    );

    verify(bookCollectionRepository).findById(eq(collectionId));
    verifyNoInteractions(bookCollectionMapper);
  }

  @Test
  @DisplayName("createBookCollection - Should create normal collection when no criteria provided")
  void createBookCollection_WithoutCriteria_ShouldCreateNormalCollection() {
    // Given
    BookCollectionCreate createDTO = new BookCollectionCreate(
            "New Collection",
            "A new test collection",
            true,
            Optional.empty()
    );

    NormalBookCollection newCollection = new NormalBookCollection.Builder()
            .title(createDTO.title())
            .description(createDTO.description())
            .owner(testUser)
            .isPublic(createDTO.isPublic())
            .build();

    BookCollectionShow expectedShow = new BookCollectionShow(
            1L,
            createDTO.title(),
            createDTO.description(),
            createDTO.isPublic(),
            Collections.emptyList(),
            null,
            BookCollection.CollectionType.NORMAL,
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    when(bookCollectionRepository.save(any(NormalBookCollection.class))).thenReturn(newCollection);
    when(bookCollectionMapper.toBookCollectionShow(any(AbstractBookCollection.class))).thenReturn(expectedShow);

    // When
    BookCollectionShow result = bookCollectionService.createBookCollection(createDTO, testUser);

    // Then
    assertNotNull(result);
    assertEquals(createDTO.title(), result.title());
    assertEquals(createDTO.description(), result.description());
    assertEquals(createDTO.isPublic(), result.isPublic());

    verify(bookCollectionRepository).save(any(NormalBookCollection.class));
    verify(bookCollectionMapper).toBookCollectionShow(any(AbstractBookCollection.class));

    ArgumentCaptor<NormalBookCollection> collectionCaptor = ArgumentCaptor.forClass(NormalBookCollection.class);
    verify(bookCollectionRepository).save(collectionCaptor.capture());
    NormalBookCollection capturedCollection = collectionCaptor.getValue();
    assertEquals(createDTO.title(), capturedCollection.getTitle());
    assertEquals(createDTO.description(), capturedCollection.getDescription());
    assertEquals(createDTO.isPublic(), capturedCollection.isPublic());
    assertEquals(testUser, capturedCollection.getOwner());
  }

  @Test
  @DisplayName("createBookCollection - Should create smart collection when criteria provided")
  void createBookCollection_WithCriteria_ShouldCreateSmartCollection() {
    // Given
    BookCollectionCreate createDTO = new BookCollectionCreate(
            "Smart Collection",
            "A smart collection with criteria",
            true,
            Optional.of("title:Test")
    );

    List<Book> allBooks = Collections.singletonList(testBook);

    SmartBookCollection smartCollection = new SmartBookCollection.Builder()
            .title(createDTO.title())
            .description(createDTO.description())
            .owner(testUser)
            .isPublic(createDTO.isPublic())
            .criteria(createDTO.criteria().get())
            .build();

    BookCollectionShow expectedShow = new BookCollectionShow(
            1L,
            createDTO.title(),
            createDTO.description(),
            createDTO.isPublic(),
            Collections.emptyList(),
            null,
            BookCollection.CollectionType.SMART,
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    when(bookRepository.findAll()).thenReturn(allBooks);
    when(bookCollectionRepository.save(any(SmartBookCollection.class))).thenReturn(smartCollection);
    when(bookCollectionMapper.toBookCollectionShow(any(AbstractBookCollection.class))).thenReturn(expectedShow);

    // When
    BookCollectionShow result = bookCollectionService.createBookCollection(createDTO, testUser);

    // Then
    assertNotNull(result);
    assertEquals(createDTO.title(), result.title());
    assertEquals(createDTO.description(), result.description());
    assertEquals(createDTO.isPublic(), result.isPublic());

    verify(bookRepository).findAll();
    verify(bookCollectionRepository).save(any(SmartBookCollection.class));
    verify(bookCollectionMapper).toBookCollectionShow(any(AbstractBookCollection.class));
  }

  @Test
  @DisplayName("updateBookCollection - Should update and return updated collection")
  void updateBookCollection_WithValidUpdate_ShouldUpdateAndReturnCollection() {
    // Given
    Long collectionId = testCollection.getId();
    String updatedTitle = "Updated Title";
    String updatedDescription = "Updated description";
    boolean updatedPublic = false;

    BookCollectionUpdate updateDTO = new BookCollectionUpdate(
            Optional.of(updatedTitle),
            Optional.of(updatedDescription),
            updatedPublic
    );

    AbstractBookCollection updatedCollection = new NormalBookCollection.Builder()
            .title(updatedTitle)
            .description(updatedDescription)
            .owner(testUser)
            .isPublic(updatedPublic)
            .build();
    updatedCollection.setId(collectionId);

    BookCollectionShow expectedShow = new BookCollectionShow(
            collectionId,
            updatedTitle,
            updatedDescription,
            updatedPublic,
            Collections.emptyList(),
            null,
            BookCollection.CollectionType.NORMAL,
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    when(bookCollectionRepository.findById(eq(collectionId))).thenReturn(Optional.of(testCollection));
    when(bookCollectionRepository.save(any(AbstractBookCollection.class))).thenReturn(updatedCollection);
    when(bookCollectionMapper.toBookCollectionShow(any(AbstractBookCollection.class))).thenReturn(expectedShow);

    // When
    BookCollectionShow result = bookCollectionService.updateBookCollection(collectionId, updateDTO);

    // Then
    assertNotNull(result);
    assertEquals(updatedTitle, result.title());
    assertEquals(updatedDescription, result.description());
    assertEquals(updatedPublic, result.isPublic());

    verify(bookCollectionRepository).findById(eq(collectionId));
    verify(bookCollectionRepository).save(any(AbstractBookCollection.class));
    verify(bookCollectionMapper).toBookCollectionShow(any(AbstractBookCollection.class));

    ArgumentCaptor<AbstractBookCollection> collectionCaptor = ArgumentCaptor.forClass(AbstractBookCollection.class);
    verify(bookCollectionRepository).save(collectionCaptor.capture());
    AbstractBookCollection capturedCollection = collectionCaptor.getValue();
    assertEquals(updatedTitle, capturedCollection.getTitle());
    assertEquals(updatedDescription, capturedCollection.getDescription());
    assertEquals(updatedPublic, capturedCollection.isPublic());
  }

  @Test
  @DisplayName("updateBookCollection - Should throw ResourceNotFoundException when collection doesn't exist")
  void updateBookCollection_WithNonExistingId_ShouldThrowException() {
    // Given
    Long collectionId = 999L;
    BookCollectionUpdate updateDTO = new BookCollectionUpdate(
            Optional.of("Updated Title"),
            Optional.empty(),
            false
    );

    when(bookCollectionRepository.findById(eq(collectionId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            bookCollectionService.updateBookCollection(collectionId, updateDTO)
    );

    verify(bookCollectionRepository).findById(eq(collectionId));
    verify(bookCollectionRepository, never()).save(any(AbstractBookCollection.class));
  }

  @Test
  @DisplayName("deleteBookCollection - Should delete existing collection")
  void deleteBookCollection_WithValidId_ShouldDeleteCollection() {
    // Given
    Long collectionId = testCollection.getId();

    when(bookCollectionRepository.findById(eq(collectionId))).thenReturn(Optional.of(testCollection));
    doNothing().when(bookCollectionRepository).delete(any(AbstractBookCollection.class));

    // When
    bookCollectionService.deleteBookCollection(collectionId);

    // Then
    verify(bookCollectionRepository).findById(eq(collectionId));
    verify(bookCollectionRepository).delete(eq(testCollection));
  }

  @Test
  @DisplayName("deleteBookCollection - Should throw ResourceNotFoundException when collection doesn't exist")
  void deleteBookCollection_WithNonExistingId_ShouldThrowException() {
    // Given
    Long collectionId = 999L;

    when(bookCollectionRepository.findById(eq(collectionId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            bookCollectionService.deleteBookCollection(collectionId)
    );

    verify(bookCollectionRepository).findById(eq(collectionId));
    verify(bookCollectionRepository, never()).delete(any(AbstractBookCollection.class));
  }

  @Test
  @DisplayName("getPublicCollections - Should return page of public collections")
  void getPublicCollections_ShouldReturnPublicCollections() {
    // Given
    Pageable pageable = Pageable.ofSize(10);

    List<AbstractBookCollection> collectionList = Collections.singletonList(testCollection);
    Page<AbstractBookCollection> collectionPage = new PageImpl<>(collectionList);

    BookCollectionIndex collectionIndex = new BookCollectionIndex(
            testCollection.getId(),
            testCollection.getTitle(),
            testCollection.getDescription(),
            testUser.getFirstName() + " " + testUser.getLastName(),
            testUser.getId(),
            0,
            testCollection.isPublic(),
            BookCollection.CollectionType.NORMAL
    );

    when(bookCollectionRepository.findByIsPublicTrue(eq(pageable))).thenReturn(collectionPage);
    when(bookCollectionMapper.toBookCollectionIndex(any(AbstractBookCollection.class))).thenReturn(collectionIndex);

    // When
    Page<BookCollectionIndex> result = bookCollectionService.getPublicCollections(pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(collectionIndex, result.getContent().get(0));

    verify(bookCollectionRepository).findByIsPublicTrue(eq(pageable));
    verify(bookCollectionMapper).toBookCollectionIndex(eq(testCollection));
  }

  @Test
  @DisplayName("addBookToCollection - Should add book to collection")
  void addBookToCollection_ShouldAddBookToCollection() {
    // Given
    Long collectionId = 1L;
    Long bookId = testBook.getId();

    // Need to use a concrete implementation that implements BookCollection interface
    NormalBookCollection collection = new NormalBookCollection.Builder()
            .title("Test Collection")
            .description("A test collection")
            .owner(testUser)
            .isPublic(true)
            .build();
    collection.setId(collectionId);

    when(bookCollectionRepository.findByIdWithBooks(eq(collectionId)))
            .thenReturn(Optional.of(collection));

    when(bookRepository.findById(eq(bookId)))
            .thenReturn(Optional.of(testBook));

    // When
    bookCollectionService.addBookToCollection(collectionId, bookId);

    // Then
    verify(bookCollectionRepository).findByIdWithBooks(eq(collectionId));
    verify(bookRepository).findById(eq(bookId));

    // Verify that the book was added to the collection
    assertTrue(collection.getBooks().contains(testBook));
  }

  @Test
  @DisplayName("addBookToCollection - Should throw ResourceNotFoundException when book doesn't exist")
  void addBookToCollection_WithNonExistingBook_ShouldThrowException() {
    // Given
    Long collectionId = 1L;
    Long bookId = 999L;

    NormalBookCollection collection = new NormalBookCollection.Builder()
            .title("Test Collection")
            .description("A test collection")
            .owner(testUser)
            .isPublic(true)
            .build();
    collection.setId(collectionId);

    when(bookCollectionRepository.findByIdWithBooks(eq(collectionId)))
            .thenReturn(Optional.of(collection));

    when(bookRepository.findById(eq(999L))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            bookCollectionService.addBookToCollection(collectionId, bookId)
    );

    verify(bookCollectionRepository).findByIdWithBooks(eq(collectionId));
    verify(bookRepository).findById(eq(bookId));
  }

  @Test
  @DisplayName("removeBookFromCollection - Should remove book from collection")
  void removeBookFromCollection_ShouldRemoveBookFromCollection() {
    // Given
    Long collectionId = 1L;
    Long bookId = testBook.getId();

    NormalBookCollection collection = new NormalBookCollection.Builder()
            .title("Test Collection")
            .description("A test collection")
            .owner(testUser)
            .isPublic(true)
            .build();
    collection.setId(collectionId);

    // Add the book to the collection first
    collection.addBook(testBook);

    when(bookCollectionRepository.findByIdWithBooks(eq(collectionId)))
            .thenReturn(Optional.of(collection));

    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));

    // When
    bookCollectionService.removeBookFromCollection(collectionId, bookId);

    // Then
    verify(bookCollectionRepository).findByIdWithBooks(eq(collectionId));
    verify(bookRepository).findById(eq(bookId));

    // Verify that the book was removed from the collection
    assertFalse(collection.getBooks().contains(testBook));
  }

  @Test
  @DisplayName("getUserCollectionsWithBooks - Should return user collections with books")
  void getUserCollectionsWithBooks_ShouldReturnUserCollectionsWithBooks() {
    // Given
    Pageable pageable = Pageable.ofSize(10);

    List<AbstractBookCollection> collections = Collections.singletonList(testCollection);

    BookCollectionShow collectionShow = new BookCollectionShow(
            testCollection.getId(),
            testCollection.getTitle(),
            testCollection.getDescription(),
            testCollection.isPublic(),
            Collections.emptyList(),
            null,
            BookCollection.CollectionType.NORMAL,
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    when(bookCollectionRepository.findByOwnerWithBooks(eq(testUser))).thenReturn(collections);
    when(bookCollectionMapper.toBookCollectionShow(any(AbstractBookCollection.class))).thenReturn(collectionShow);

    // When
    Page<BookCollectionShow> result = bookCollectionService.getUserCollectionsWithBooks(testUser, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(collectionShow, result.getContent().get(0));

    verify(bookCollectionRepository).findByOwnerWithBooks(eq(testUser));
    verify(bookCollectionMapper).toBookCollectionShow(eq(testCollection));
  }

  @Test
  @DisplayName("getPublicUserCollection - Should return public collections of a user")
  void getPublicUserCollection_ShouldReturnPublicUserCollections() {
    // Given
    Pageable pageable = Pageable.ofSize(10);

    List<AbstractBookCollection> collectionList = Collections.singletonList(testCollection);
    Page<AbstractBookCollection> collectionPage = new PageImpl<>(collectionList);

    BookCollectionIndex collectionIndex = new BookCollectionIndex(
            testCollection.getId(),
            testCollection.getTitle(),
            testCollection.getDescription(),
            testUser.getFirstName() + " " + testUser.getLastName(),
            testUser.getId(),
            0,
            testCollection.isPublic(),
            BookCollection.CollectionType.NORMAL
    );

    when(bookCollectionRepository.findPublicCollectionsByOwner(eq(testUser), eq(pageable))).thenReturn(collectionPage);
    when(bookCollectionMapper.toBookCollectionIndex(any(AbstractBookCollection.class))).thenReturn(collectionIndex);

    // When
    Page<BookCollectionIndex> result = bookCollectionService.getPublicUserCollection(testUser, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(collectionIndex, result.getContent().get(0));

    verify(bookCollectionRepository).findPublicCollectionsByOwner(eq(testUser), eq(pageable));
    verify(bookCollectionMapper).toBookCollectionIndex(eq(testCollection));
  }

  @Test
  @DisplayName("getPrivateUserCollection - Should return private collections of a user")
  void getPrivateUserCollection_ShouldReturnPrivateUserCollections() {
    // Given
    Pageable pageable = Pageable.ofSize(10);

    List<AbstractBookCollection> collectionList = Collections.singletonList(testCollection);
    Page<AbstractBookCollection> collectionPage = new PageImpl<>(collectionList);

    BookCollectionIndex collectionIndex = new BookCollectionIndex(
            testCollection.getId(),
            testCollection.getTitle(),
            testCollection.getDescription(),
            testUser.getFirstName() + " " + testUser.getLastName(),
            testUser.getId(),
            0,
            testCollection.isPublic(),
            BookCollection.CollectionType.NORMAL
    );

    when(bookCollectionRepository.findPrivateCollectionsByOwner(eq(testUser), eq(pageable))).thenReturn(collectionPage);
    when(bookCollectionMapper.toBookCollectionIndex(any(AbstractBookCollection.class))).thenReturn(collectionIndex);

    // When
    Page<BookCollectionIndex> result = bookCollectionService.getPrivateUserCollection(testUser, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(collectionIndex, result.getContent().get(0));

    verify(bookCollectionRepository).findPrivateCollectionsByOwner(eq(testUser), eq(pageable));
    verify(bookCollectionMapper).toBookCollectionIndex(eq(testCollection));
  }
}