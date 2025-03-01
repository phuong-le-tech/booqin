package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.entity.User;
import org.springframework.security.core.Authentication;

/**
 * Service responsible for authentication operations
 */
public interface AuthServiceInterface {

  /**
   * Register a new user
   *
   * @param registerDto Registration data
   * @return The created user
   * @throws IllegalArgumentException if email is already taken
   */
  User register(RegisterDto registerDto);

  /**
   * Authenticate a user and generate JWT tokens
   *
   * @param loginDto Login credentials
   * @return Authentication response containing access and refresh tokens
   * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
   */
  AuthResponse login(LoginDto loginDto);

  /**
   * Get current authenticated user information
   *
   * @param authentication Current authentication context
   * @return DTO with user information
   * @throws IllegalStateException if user is not authenticated
   */
  UserMeDto getCurrentUser(Authentication authentication);
}