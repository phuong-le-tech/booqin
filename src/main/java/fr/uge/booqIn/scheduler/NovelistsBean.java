package fr.uge.booqIn.scheduler;

import fr.uge.booqIn.repository.BookService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

@Component
public class NovelistsBean implements InitializingBean {
  private static final Logger logger = Logger.getLogger(NovelistsBean.class.getName());
  private static final String FILE_PATH = "src/main/java/fr/uge/booqIn/scheduler/novelists.txt";
  private List<String> novelists;



  @Override
  public void afterPropertiesSet() throws Exception {
    try (var stream = Files.lines(Paths.get(FILE_PATH))) {
      novelists = stream.toList();
    } catch (IOException ioe) {
      logger.severe("Error initializing authors: " + ioe.getMessage());
      throw new RuntimeException("Failed to initialize novelists", ioe);
    }
  }

  public List<String> getNovelists() {
    return novelists;
  }
}