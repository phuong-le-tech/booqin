package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.specification.BookCollectionSpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class BookCollectionServiceImpl implements BookCollectionServiceInterface {
  private static final Logger logger = Logger.getLogger(BookCollectionServiceImpl.class.getName());
  private final BookCollectionRepository bookCollectionRepository;
  private final BookRepository bookRepository;
  private final BookCollectionMapper bookCollectionMapper;

  public BookCollectionServiceImpl(BookCollectionRepository bookCollectionRepository,
                                   BookRepository bookRepository,
                                   BookCollectionMapper bookCollectionMapper) {
    this.bookCollectionRepository = bookCollectionRepository;
    this.bookRepository = bookRepository;
    this.bookCollectionMapper = bookCollectionMapper;
  }

  @Override
  @Transactional
  public Page<BookCollectionIndex> findBookCollections(String name, String description, Long userId, Pageable pageable) {
    // Build the specification for filtering
    Specification<BookCollection> spec = Specification.where(null);

    if (name != null && !name.isEmpty()) {
      spec = spec.and(BookCollectionSpecification.hasTitle(name));
    }

    if (description != null && !description.isEmpty()) {
      spec = spec.and(BookCollectionSpecification.hasDescription(description));
    }

    if (userId != null) {
      spec = spec.and(BookCollectionSpecification.hasOwner(userId));
    }

    return bookCollectionRepository.findAll(spec, pageable)
            .map(bookCollectionMapper::toBookCollectionIndex);
  }

  @Override
  @Transactional
  public BookCollectionShow findById(@NotNull Long id) {
    BookCollection bookCollection = findBookCollectionById(id);
    return bookCollectionMapper.toBookCollectionShow(bookCollection);
  }

  @Override
  @Transactional
  public BookCollection createBookCollection(@NotNull @Valid BookCollection bookCollection) {
    if (bookCollection.getBooks() != null) {
      bookCollection.getBooks().forEach(book -> {
        if (book.getId() != null && !bookRepository.existsById(book.getId())) {
          throw new ResourceNotFoundException("Book", "id", book.getId());
        }
      });
    }

    bookCollection.setCreatedAt(new Date());
    bookCollection.setUpdatedAt(new Date());
    return bookCollectionRepository.save(bookCollection);
  }

  @Override
  @Transactional
  public BookCollection updateBookCollection(@NotNull @Valid BookCollection bookCollection) {
    if (!bookCollectionRepository.existsById(bookCollection.getId())) {
      throw new ResourceNotFoundException("BookCollection", "id", bookCollection.getId());
    }

    if (bookCollection.getBooks() != null) {
      bookCollection.getBooks().forEach(book -> {
        if (book.getId() != null && !bookRepository.existsById(book.getId())) {
          throw new ResourceNotFoundException("Book", "id", book.getId());
        }
      });
    }

    bookCollection.setUpdatedAt(new Date());
    return bookCollectionRepository.save(bookCollection);
  }

  @Override
  @Transactional
  public BookCollectionShow patchBookCollection(@NotNull Long id, @NotNull @Valid BookCollectionUpdate bookCollectionUpdate) {
    BookCollection bookCollection = findBookCollectionById(id);

    bookCollectionUpdate.name().ifPresent(bookCollection::setTitle);
    bookCollectionUpdate.description().ifPresent(bookCollection::setDescription);

    bookCollectionUpdate.books().ifPresent(books -> {
      books.forEach(book -> {
        if (book.getId() != null && !bookRepository.existsById(book.getId())) {
          throw new ResourceNotFoundException("Book", "id", book.getId());
        }
      });
      bookCollection.setBooks(books);
    });

    bookCollection.setUpdatedAt(new Date());
    return bookCollectionMapper.toBookCollectionShow(bookCollectionRepository.save(bookCollection));
  }

  @Override
  @Transactional
  public void deleteBookCollection(@NotNull Long id) {
    BookCollection bookCollection = findBookCollectionById(id);
    bookCollectionRepository.delete(bookCollection);
  }

  private BookCollection findBookCollectionById(Long id) {
    return bookCollectionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("BookCollection", "id", id));
  }

  @Transactional
  public List<BookCollection> findAll() {
    return bookCollectionRepository.findAll();
  }
}