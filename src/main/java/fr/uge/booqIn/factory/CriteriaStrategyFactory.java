package fr.uge.booqIn.factory;

import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.strategy.collection.AuthorCriteriaStrategy;
import fr.uge.booqIn.strategy.collection.CollectionCriteriaStrategy;
import fr.uge.booqIn.strategy.collection.PublicationDateCriteriaStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Factory component for creating {@link CollectionCriteriaStrategy} instances based on a map of criteria.
 * This factory allows the creation of combined criteria strategies to filter books based on multiple conditions.
 *
 * <p>Each entry in the criteria map represents a specific condition (e.g., author name, maximum publication age).
 * The factory creates individual strategies for each condition and combines them into a single strategy
 * using a logical AND operation (all conditions must be satisfied).</p>
 *
 * <p>If an unsupported criteria key is provided, the factory returns a default strategy that accepts all books.</p>
 *
 * @see CollectionCriteriaStrategy
 */
@Component
public class CriteriaStrategyFactory {
  /**
   * Creates a combined {@link CollectionCriteriaStrategy} based on the provided criteria map.
   *
   * <p>Each entry in the map is converted into an individual strategy, and all strategies are combined
   * using a logical AND operation. If no criteria are provided, the resulting strategy accepts all books.</p>
   *
   * @param bookCriteriaMap A map of criteria where the key is the condition type (e.g., "author") and the value
   *                        is the condition value (e.g., "Michael Connelly").
   * @return A combined {@link CollectionCriteriaStrategy} that evaluates all criteria.
   */
  // Object here because we can have a criteria for the author name, for the publication data, etc.. so different types
  public CollectionCriteriaStrategy createCriteriaStrategy(Map<String, Object> bookCriteriaMap) {
    return bookCriteriaMap.entrySet().stream().map(this::createSingleStrategy)
            .reduce(book -> true, CollectionCriteriaStrategy::combineCriteria);
  }

  /**
   * Creates an individual {@link CollectionCriteriaStrategy} for a single criteria entry.
   *
   * <p>This method determines the type of strategy to create based on the criteria key. If the key is not recognized,
   * a default strategy that accepts all books is returned.</p>
   *
   * @param entry A map entry representing a single criterion (key-value pair).
   * @return A {@link CollectionCriteriaStrategy} corresponding to the criterion.
   */
  private CollectionCriteriaStrategy createSingleStrategy(Map.Entry<String, Object> entry) {
    return switch (entry.getKey()) {
      case "author" -> new AuthorCriteriaStrategy((Set<Author>) entry.getValue());
      case "maxYearsOld" -> new PublicationDateCriteriaStrategy((Integer) entry.getValue());
      default -> book -> true;
    };
  }
}
