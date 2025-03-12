package fr.uge.booqIn.strategy.votable;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.strategy.VotableHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookVotableHandler implements VotableHandler<Book> {

    private static final String TYPE = "BOOK";
    private final BookRepository bookRepository;

    public BookVotableHandler(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public String getVotableType() {
        return TYPE;
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public boolean canHandle(String votableType) {
        return TYPE.equalsIgnoreCase(votableType);
    }
}