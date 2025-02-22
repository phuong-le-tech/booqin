package fr.uge.booqIn.config;

import fr.uge.booqIn.filter.JwtAuthFilter;
import fr.uge.booqIn.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

 @Bean
  public SecurityFilterChain securityFilterChain(
          HttpSecurity http,
          JwtAuthFilter jwtAuthFilter,
          AuthenticationProvider authenticationProvider
  ) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            /*.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/h2-console/**").permitAll() //TODO Db accessible que en admin
                    .requestMatchers("/auth/**", "/home", "/favicon.ico", "/static/**").permitAll()
                    .requestMatchers(
                            "/api/v1/auth/register",
                            "/api/v1/auth/login"
                    ).permitAll()
                    .anyRequest().authenticated()
            )*/
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(login -> login
                    .loginPage("/auth/login")
                    .loginProcessingUrl("/auth/login")
                    .defaultSuccessUrl("/home", true)
                    .failureUrl("/auth/login?error=true")
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/home")
                    .permitAll()
            )
            .headers(headers -> headers.frameOptions().disable()); // sinon le navigateur bloque l'affichage de la console h2 mais a change
    ;

    return http.build();
  }

  @Bean
  @Order(1)
  public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                    JwtAuthFilter jwtAuthFilter,
                                                    AuthenticationProvider authenticationProvider) throws Exception {
    http
            // On applique cette config seulement pour les URLs commençant par /api/
            .securityMatcher("/api/**")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/**", "/home", "/favicon.ico", "/static/**").permitAll()
                    .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                    .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions().disable());

    return http.build();
  }

  /**
   * 2) SecurityFilterChain pour l'application Thymeleaf,
   *    en mode STATEFUL + session classique.
   */
  @Bean
  @Order(2)
  public SecurityFilterChain webSecurityFilterChain(HttpSecurity http,
                                                    AuthenticationProvider authenticationProvider) throws Exception {
    http
            // On applique cette config pour tout le reste (/**), sauf /api/** qui matche déjà au-dessus
            .securityMatcher("/**")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/**", "/home", "/h2-console/**", "/static/**", "/favicon.ico").permitAll()
                    .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authenticationProvider(authenticationProvider)
            .formLogin(login -> login
                    .loginPage("/auth/login")
                    .loginProcessingUrl("/auth/login")
                    .defaultSuccessUrl("/home", true)
                    .failureUrl("/auth/login?error=true")
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout") // ou "/auth/logout"
                    .logoutSuccessUrl("/home")
                    .permitAll()
            )
            .headers(headers -> headers.frameOptions().disable());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider(
          UserDetailsServiceImpl userDetailsServiceImpl,
          PasswordEncoder passwordEncoder
  ) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsServiceImpl);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(
          AuthenticationConfiguration config
  ) throws Exception {
    return config.getAuthenticationManager();
  }
}
