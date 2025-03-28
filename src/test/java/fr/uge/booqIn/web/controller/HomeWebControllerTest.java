package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.HomeWebController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("HomeWebController Tests")
class HomeWebControllerTest {

  private MockMvc mockMvc;

  @Mock
  private Principal principal;

  @Mock
  private Model model;

  @InjectMocks
  private HomeWebController homeWebController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/templates/");
    viewResolver.setSuffix(".html");

    mockMvc = MockMvcBuilders.standaloneSetup(homeWebController)
            .setViewResolvers(viewResolver)
            .build();

    // Set up default behaviors for mocks
    when(principal.getName()).thenReturn("testuser");
  }

  @Test
  @DisplayName("/ - Should return home page")
  void home_ShouldReturnHomePage() throws Exception {
    // When & Then
    mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("title"))
            .andExpect(model().attribute("title", "BooqIn - Home"));
  }

  @Test
  @DisplayName("/home - Should return home page")
  void homeAlias_ShouldReturnHomePage() throws Exception {
    // When & Then
    mockMvc.perform(get("/home"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("title"))
            .andExpect(model().attribute("title", "BooqIn - Home"));
  }
}
