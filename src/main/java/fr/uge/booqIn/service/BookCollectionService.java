package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.BookCollection;

import fr.uge.booqIn.repository.BookCollectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class BookCollectionService {

    private final BookCollectionRepository bookCollectionRepository;

    public BookCollectionService(BookCollectionRepository bookCollectionRepository) {
        this.bookCollectionRepository = bookCollectionRepository;
    }

    public Optional<BookCollection> getCollectionById(Long id) {
        return bookCollectionRepository.findById(id);
    }

}
