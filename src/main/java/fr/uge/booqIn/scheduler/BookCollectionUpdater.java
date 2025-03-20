package fr.uge.booqIn.scheduler;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.SmartBookCollection;
import fr.uge.booqIn.factory.CriteriaStrategyFactory;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class BookCollectionUpdater {
  private static final Logger logger = Logger.getLogger(BookCollectionUpdater.class.getName());
  private final BookCollectionRepository bookCollectionRepository;
  private final BookRepository bookRepository;
  private final CriteriaStrategyFactory criteriaFactory;

  public BookCollectionUpdater(
          BookCollectionRepository bookCollectionRepository,
          CriteriaStrategyFactory criteriaFactory,
          BookRepository bookRepository) {
    this.bookCollectionRepository = bookCollectionRepository;
    this.criteriaFactory = criteriaFactory;
    this.bookRepository = bookRepository;
  }

  @Scheduled(cron = "0 0 0 * * ?")
  @Transactional
  public void updateAllSmartCollections() {
    List<SmartBookCollection> smartCollections = bookCollectionRepository.findAllSmartCollections();
    for (SmartBookCollection collection : smartCollections) {
      try {
        updateSmartCollectionBooks(collection);
      } catch (Exception e) {
        System.err.println("Error updating smart collection ID " + collection.getId() + ": " + e.getMessage());
      }
    }
  }

  private void updateSmartCollectionBooks(SmartBookCollection collection) {
    if (collection.getCriteriaStrategy() == null) {
      collection.initializeCriteriaStrategy(criteriaFactory);
    }
    List<Book> allBooks = bookRepository.findAll();
    collection.updateBooks(allBooks);
    bookCollectionRepository.save(collection);
  }

}
