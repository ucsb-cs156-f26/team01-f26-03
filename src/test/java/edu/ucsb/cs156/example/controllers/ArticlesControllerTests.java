package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockitoBean ArticlesRepository articlesRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/articles/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(200));
  }

  // Authorization tests for /api/articles/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "titletitle")
                .param("url", "urlurl")
                .param("explanation", "expexp")
                .param("email", "a@b.com")
                .param("dateAdded", "2026-01-01T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
    verifyNoInteractions(articlesRepository);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "t")
                .param("url", "u")
                .param("explanation", "e")
                .param("email", "a@b.com")
                .param("dateAdded", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
    verifyNoInteractions(articlesRepository);
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

    Articles article1 =
        Articles.builder()
            .title("First")
            .url("https://abc.com/1")
            .explanation("first article")
            .email("one@ucsb.edu")
            .dateAdded(ldt1)
            .build();

    Articles article2 =
        Articles.builder()
            .title("Second")
            .url("https://abc.com/2")
            .explanation("second article")
            .email("two@ucsb.edu")
            .dateAdded(ldt2)
            .build();

    ArrayList<Articles> expectedArticles = new ArrayList<>(Arrays.asList(article1, article2));

    when(articlesRepository.findAll()).thenReturn(expectedArticles);

    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    verify(articlesRepository, org.mockito.Mockito.times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Articles article1 =
        Articles.builder()
            .title("First")
            .url("https://example.com/1")
            .explanation("first article")
            .email("one@ucsb.edu")
            .dateAdded(ldt1)
            .build();

    when(articlesRepository.save(article1)).thenReturn(article1);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post")
                    .param("title", "First")
                    .param("url", "https://example.com/1")
                    .param("explanation", "first article")
                    .param("email", "one@ucsb.edu")
                    .param("dateAdded", "2022-01-03T00:00:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository).save(article1);
    String expectedJson = mapper.writeValueAsString(article1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
