package fr.uge.booqIn.factory;

import fr.uge.booqIn.entity.CriteriaEntry;
import fr.uge.booqIn.strategy.collection.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Factory for creating collection criteria strategies based on criteria entries
 */
@Component
public class CriteriaStrategyFactory {
  public CollectionCriteriaStrategy createCriteriaStrategy(String criteriaString) {
    if (criteriaString == null || criteriaString.isEmpty()) {
      return book -> true;
    }

    List<CriteriaEntry> criteriaEntries = parseCriteriaString(criteriaString);

    Map<String, List<CriteriaEntry>> criteriaByKey = criteriaEntries.stream()
            .collect(Collectors.groupingBy(CriteriaEntry::key));

    List<CollectionCriteriaStrategy> strategies = new ArrayList<>();

    // Process author criteria by name
    if (criteriaByKey.containsKey("author")) {
      criteriaByKey.get("author").forEach(entry -> {
        String authorName = entry.value();
        if (authorName != null && !authorName.isEmpty()) {
          strategies.add(new AuthorCriteriaStrategy(authorName));
        }
      });
    }

    if (criteriaByKey.containsKey("publicationDate")) {
      criteriaByKey.get("publicationDate").stream()
              .findFirst()
              .ifPresent(entry -> {
                try {
                  int maxYear = Integer.parseInt(entry.value());
                  strategies.add(new PublicationDateCriteriaStrategy(maxYear));
                } catch (NumberFormatException e) {
                  // Ignore invalid year format
                }
              });
    }

    if (criteriaByKey.containsKey("recent")) {
      int days = 30;
      try {
        days = Integer.parseInt(criteriaByKey.get("recent").get(0).value());
      } catch (Exception e) {
        // Use default if parsing fails
      }
      final int daysThreshold = days;

      strategies.add(book -> {
        if (book.getCreatedAt() == null) {
          return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysThreshold);
        Date thresholdDate = calendar.getTime();
        return book.getCreatedAt().after(thresholdDate);
      });
    }

    return strategies.stream()
            .reduce(book -> true,
                    CollectionCriteriaStrategy::combineCriteria);
  }

  /**
   * Parse a criteria string representation and build a list of criteria entries
   * Format: "key1:value1;key2:value2;..."
   *
   * @param criteriaString The criteria string
   * @return A list of criteria entries
   */
  public List<CriteriaEntry> parseCriteriaString(String criteriaString) {
    if (criteriaString == null || criteriaString.isEmpty()) {
      return Collections.emptyList();
    }

    List<CriteriaEntry> entries = new ArrayList<>();
    String[] criteriaPairs = criteriaString.split(";");

    for (String pair : criteriaPairs) {
      String[] keyValue = pair.split(":");
      if (keyValue.length == 2) {
        entries.add(new CriteriaEntry(keyValue[0].trim(), keyValue[1].trim()));
      }
    }

    return entries;
  }
}