package fr.uge.booqIn.scheduler;

import java.util.List;

public class AuthorIterator {
  private final NovelistsBean novelistsBean;
  private int currentIndex = 0;
  private final int batchSize;

  public AuthorIterator(NovelistsBean novelistsBean, int batchSize) {
    this.novelistsBean = novelistsBean;
    this.batchSize = batchSize;
  }

  public List<String> getNextBatch() {
    List<String> authors = novelistsBean.getNovelists();
    if (authors.isEmpty()) {
      return List.of();
    }
    int endIndex = Math.min(currentIndex + batchSize, authors.size());
    List<String> batch = authors.subList(currentIndex, endIndex);
    currentIndex = endIndex % authors.size();
    return batch;
  }
}