package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.repository.BookCollectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
@Service
public class BookCollectionService {

    private final BookCollectionRepository bookCollectionRepository;



    public BookCollectionService(BookCollectionRepository bookCollectionRepository) {
        this.bookCollectionRepository = bookCollectionRepository;
    }


    public Optional<BookCollection> getCollectionById(Long id) {
        return bookCollectionRepository.findById(id);
    }


}
