package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for authentication related objects
 */
@Component
public class AuthMapper {

  /**
   * Maps User entity to UserMeDto
   *
   * @param user User entity
   * @return UserMeDto with basic user information
   */
  public UserMeDto toUserMeDto(User user) {
    return new UserMeDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole().name()
    );
  }
}