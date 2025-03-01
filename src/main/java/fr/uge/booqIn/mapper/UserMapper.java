package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  /**
   * Maps User entity to UserIndex DTO
   */
  public UserIndex toUserIndex(User user) {
    return new UserIndex(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail()
    );
  }

  /**
   * Maps User entity to UserShow DTO
   * Note: the collections must be initialized before calling this method
   */
  public UserShow toUserShow(User user) {
    return new UserShow(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getAddress(),
            user.getCollections()
    );
  }
}