package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.model.SmartCollectionDTORequest;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.CriteriaStrategyFactory;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.CollectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmartCollectionService {
  private final CollectionRepository collectionRepository;
  private final BookRepository bookRepository;
  private final CriteriaStrategyFactory strategyFactory;

  public SmartCollectionService(CollectionRepository collectionRepository, BookRepository bookRepository, CriteriaStrategyFactory strategyFactory) {
    this.collectionRepository = collectionRepository;
    this.bookRepository = bookRepository;
    this.strategyFactory = strategyFactory;
  }

  // create/update into a smart collection
  @Transactional
  public BookCollection createSmartCollection(BookCollection bookCollection) {
    bookCollection.setSmart(true);
    return collectionRepository.save(bookCollection);
  }

  @Transactional
  public BookCollection createSmartCollection(User owner, SmartCollectionDTORequest request) {
    var strategy = strategyFactory.createCriteriaStrategy(request.criteria());
    var collection = new BookCollection();  // on va créer la collection personnalisée correspondante
    collection.setOwner(owner);
    collection.setTitle(request.title());
    collection.setDescription(request.description());
    collection.setPublic(request.isPublic());
    collection.setSmart(true);
    // on va remplir cette collection avec les livres correspondant à la stratégie
    var matchingBooks = bookRepository.findAll().stream().filter(strategy::satisfy).collect(Collectors.toList());
    collection.setBooks(matchingBooks);
    return collectionRepository.save(collection);
  }

  @Scheduled(fixedRate = 24 * 3600 * 1000)  // every day
  public void updateSmartCollections() {
    var smartCollections = collectionRepository.findByIsSmart(true);
    smartCollections.stream().map(this::createSmartCollection).forEach(collectionRepository::save);
  }

  @Transactional
  public List<BookCollection> getSmartCollections() {
    List<BookCollection> smartCollections = collectionRepository.findByIsSmart(true);
    //List<BookCollection> smartCollections = collectionRepository.findSmartCollections();
    if (smartCollections.isEmpty()) {
      throw new IllegalArgumentException("No smart collections found");
    }
    return smartCollections;
  }
}
