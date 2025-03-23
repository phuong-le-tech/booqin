package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.entity.User;
import org.springframework.security.core.Authentication;

public interface AuthServiceInterface {

  /**
   * Registers a new user
   * Handles concurrent registration attempts for the same email
   *
   * @param registerDto the registration data
   * @return the created user
   * @throws fr.uge.booqIn.exception.DuplicateResourceException if email already exists
   */
  User register(RegisterDto registerDto);

  /**
   * Authenticates a user and returns JWT tokens
   *
   * @param loginDto the login credentials
   * @return AuthResponse containing access and refresh tokens
   * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
   */
  AuthResponse login(LoginDto loginDto);

  /**
   * Retrieves current user data from the authentication context
   *
   * @param authentication the current authentication context
   * @return UserMeDto containing user data
   * @throws IllegalStateException if user is not authenticated
   */
  UserMeDto getCurrentUser(Authentication authentication);


  /**
   * Invalidates all sessions for a user by forcing version increment
   *
   * @param userId the user ID
   * @throws IllegalStateException if user not found
   */
  void invalidateUserSessions(Long userId);
}