package fr.uge.booqIn.strategy.handlers;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookEntityHandler implements EntityHandler<Book> {

    private final BookRepository bookRepository;

    public BookEntityHandler(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public String getEntityType() {
        return "BOOK";
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }
}