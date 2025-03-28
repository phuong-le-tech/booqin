package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

  private final BookCollectionMapper bookCollectionMapper;

  public UserMapper(BookCollectionMapper bookCollectionMapper) {
    this.bookCollectionMapper = bookCollectionMapper;
  }

  /**
   * Maps User entity to UserIndex DTO
   */
  public UserIndex toUserIndex(User user) {
    return new UserIndex(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getAddress(),
            user.getRole()
    );
  }

  /**
   * Maps User entity to UserShow DTO
   * Note: the collections must be initialized before calling this method
   */
  public UserShow toUserShow(User user) {
    List<BookCollectionIndex> collectionShowList = user.getCollections().stream()
            .map(bookCollectionMapper::toBookCollectionIndex)
            .toList();
    return new UserShow(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getAddress(),
            //List.of(),
            collectionShowList,
            user.getRole()
    );
  }

  public User userShowToUser(UserShow userShow) {
    return new User(
            userShow.firstName(),
            userShow.lastName(),
            userShow.email(),
            "",
            userShow.address(),
            userShow.role()
    );
  }

  public User userIndexToUser(UserIndex userIndex) {
    return new User(
            userIndex.firstName(),
            userIndex.lastName(),
            userIndex.email(),
            "",
            userIndex.address(),
            userIndex.role()
    );
  }
}