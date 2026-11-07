package com.css.onboarding.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.css.onboarding.persistence.OnboardingJpa;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/** Running app + PostgreSQL: the Lab 3 duplicate is now a 409, and the first row stays. */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OnboardingApiTest {

  @Autowired TestRestTemplate http;
  @Autowired JdbcTemplate jdbc;
  @Autowired OnboardingJpa onboardings;

  @BeforeEach
  void empty() {
    jdbc.update("DELETE FROM onboarding");
  }

  @Test
  void secondPostOfSameKeyIs409AndCountStaysOne() {
    ResponseEntity<Map> first = http.postForEntity("/api/v1/onboardings",
        Map.of("businessKey", "OB-7", "title", "New hire Alex"), Map.class);
    assertEquals(HttpStatus.CREATED, first.getStatusCode());

    ResponseEntity<Map> read = http.getForEntity(first.getHeaders().getLocation(), Map.class);
    assertEquals(HttpStatus.OK, read.getStatusCode());
    assertEquals("PLANNED", read.getBody().get("status"));

    ResponseEntity<Map> second = http.postForEntity("/api/v1/onboardings",
        Map.of("businessKey", "OB-7", "title", "Again"), Map.class);
    assertEquals(HttpStatus.CONFLICT, second.getStatusCode());
    assertEquals("DUPLICATE", second.getBody().get("code"));
    assertTrue(second.getBody().keySet().equals(java.util.Set.of("code", "message", "correlationId")));

    assertEquals(1, onboardings.count("OB-7"));
  }
}
