package com.css.onboarding.handler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.css.onboarding.config.Application;
import com.css.onboarding.config.OnboardingService;
import com.css.onboarding.domain.DuplicateOnboarding;
import com.css.onboarding.domain.Onboarding;
import com.css.onboarding.domain.OnboardingId;
import com.css.onboarding.domain.OnboardingNotFound;
import com.css.onboarding.domain.OnboardingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Web slice only: the service is a mock, no PostgreSQL, no real HTTP server. */
@WebMvcTest(OnboardingController.class)
@ContextConfiguration(classes = Application.class)
class OnboardingControllerTest {

  private static final String URL = "/api/v1/onboardings";

  @Autowired MockMvc mvc;
  @MockitoBean OnboardingService service;

  @Test
  void validBodyIs201WithLocation() throws Exception {
    OnboardingId id = OnboardingId.newId();
    when(service.register(any(), eq("OB-7"), eq("New hire Alex")))
        .thenReturn(new Onboarding(id, "OB-7", new OnboardingStatus.Planned(), "New hire Alex"));

    mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"businessKey": "OB-7", "title": "New hire Alex"}
                """))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", endsWith(URL + "/" + id.value())))
        .andExpect(jsonPath("$.id").value(id.value().toString()))
        .andExpect(jsonPath("$.businessKey").value("OB-7"))
        .andExpect(jsonPath("$.status").value("PLANNED"))
        .andExpect(jsonPath("$.title").value("New hire Alex"));
  }

  @ParameterizedTest(name = "{2}")
  @CsvSource(delimiter = '|', value = {
      "OB-7 | ''    | title must not be blank",
      "OB-7 | '   ' | title must not be blank",
      "''   | Alex  | business key must not be blank"
  })
  void blankFieldIs400AndServiceNotCalled(String key, String title, String message) throws Exception {
    mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
            .content("{\"businessKey\": \"" + key + "\", \"title\": \"" + title + "\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION"))
        .andExpect(jsonPath("$.message").value(message))
        .andExpect(jsonPath("$.correlationId").isNotEmpty());
    verify(service, never()).register(any(), anyString(), anyString());
  }

  @Test
  void title121Is400() throws Exception {
    mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
            .content("{\"businessKey\": \"OB-7\", \"title\": \"" + "a".repeat(121) + "\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION"))
        .andExpect(jsonPath("$.message").value(containsString("size must be between 0 and 120")));
  }

  @Test
  void title120Is201() throws Exception {
    String title = "a".repeat(120);
    when(service.register(any(), eq("OB-7"), eq(title)))
        .thenReturn(new Onboarding(OnboardingId.newId(), "OB-7", new OnboardingStatus.Planned(), title));
    mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
            .content("{\"businessKey\": \"OB-7\", \"title\": \"" + title + "\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value(title));
  }

  @Test
  void duplicateIs409() throws Exception {
    when(service.register(any(), eq("OB-7"), anyString())).thenThrow(new DuplicateOnboarding("OB-7"));
    mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"businessKey": "OB-7", "title": "Again"}
                """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE"));
  }

  @Test
  void missingIs404() throws Exception {
    OnboardingId id = OnboardingId.newId();
    when(service.get(id)).thenThrow(new OnboardingNotFound(id));
    mvc.perform(get(URL + "/" + id.value()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
  }

  @Test
  void foundIs200() throws Exception {
    OnboardingId id = OnboardingId.newId();
    when(service.get(id)).thenReturn(new Onboarding(id, "OB-7", new OnboardingStatus.Started(), "Alex"));
    mvc.perform(get(URL + "/" + id.value()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("STARTED"));
  }

  @Test
  void businessKeyInPathIs400() throws Exception {
    mvc.perform(get(URL + "/OB-7"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION"))
        .andExpect(jsonPath("$.message").value("id must be a UUID"));
  }

  @Test
  void notJsonIs400() throws Exception {
    mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{not json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("body must be JSON"));
  }

  @Test
  void wrongContentTypeIs415() throws Exception {
    mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("OB-7"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.code").value("UNSUPPORTED"));
  }

  @Test
  void unexpectedIs500WithoutLeak() throws Exception {
    OnboardingId id = OnboardingId.newId();
    when(service.get(id)).thenThrow(new IllegalStateException("secret table detail"));
    mvc.perform(get(URL + "/" + id.value()))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("INTERNAL"))
        .andExpect(jsonPath("$.message").value("unexpected error"));
  }

  @Test
  void correlationIdIsEchoed() throws Exception {
    mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
            .header("X-Correlation-Id", "abc-123")
            .content("{\"businessKey\": \"OB-7\", \"title\": \"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.correlationId").value("abc-123"));
  }
}
