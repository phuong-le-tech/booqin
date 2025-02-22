package fr.uge.booqIn.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {
  @Autowired
  private MockMvc mockMvc;

//  @Test
//  void testUnauthenticatedAccess() throws Exception {
//    mockMvc.perform(get("/api/v1/collections"))
//            .andExpect(status().isUnauthorized());
//  }
//
//  @Test
//  void testAuthenticatedAccess() throws Exception {
//    mockMvc.perform(get("/api/v1/collections")
//                    .header("Authorization", "Bearer valid-jwt-token"))
//            .andExpect(status().isOk());
//  }
}