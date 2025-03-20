package fr.uge.booqIn.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record CriteriaEntry(
        String key,
        String value
) {
  public CriteriaEntry() {
    this("", "");
  }
}
