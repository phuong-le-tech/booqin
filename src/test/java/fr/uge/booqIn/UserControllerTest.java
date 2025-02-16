package fr.uge.booqIn;

import fr.uge.booqIn.entity.User;
//import fr.uge.booqIn.entity.Person;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
/*
  @Autowired
  private MockMvc mockMvc;
  
  @Autowired
  private UserService userService;
  
  @Autowired
  private ObjectMapper objectMapper;
  
  private User testUser;
  
  @BeforeEach
  void setUp() {
    // Création d'un utilisateur test
    Address address = new Address("42", "Baker Street", "London", "NW16XE");
    Person person = new Person("Alice", "Johnson", "alice.johnson@example.com", address);
    testUser = new User(person, "P@ssw0rd123", "ADMIN");
    userService.saveUser(testUser);
  }
  
  @Test
  void testRegisterUser() throws Exception {
    // Création d'un nouvel utilisateur
    Address address = new Address("10", "Main Street", "New York", "10001");
    Person person = new Person("John", "Doe", "john.doe@example.com", address);
    User newUser = new User(person, "securepassword", "USER");
    
    ResultActions response = mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newUser)));
    
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.person.email").value("john.doe@example.com"));
  }
  
  @Test
  void testGetUserById() throws Exception {
    ResultActions response = mockMvc.perform(get("/api/users/" + testUser.getId())
            .contentType(MediaType.APPLICATION_JSON));
    
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.person.email").value("alice.johnson@example.com"));
  }
  
  @Test
  void testLazyLoadingCollections() throws Exception {
    ResultActions response = mockMvc.perform(get("/api/users/" + testUser.getId())
            .contentType(MediaType.APPLICATION_JSON));
    
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.booksToLend").doesNotExist())
            .andExpect(jsonPath("$.collections").doesNotExist());
  }

 */
}
