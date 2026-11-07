package com.css.onboarding.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** A real server on a random port: health and info are open, nothing else. */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorTest {

  @Autowired TestRestTemplate http;

  @Test
  void healthIsUpWithoutDetails() {
    ResponseEntity<String> res = http.getForEntity("/actuator/health", String.class);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().contains("\"status\":\"UP\""));
    assertFalse(res.getBody().contains("jdbc"), "health must not list the datasource");
  }

  @Test
  void infoNamesTheProduct() {
    ResponseEntity<String> res = http.getForEntity("/actuator/info", String.class);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().contains("Onboarding"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"env", "beans", "heapdump", "shutdown"})
  void otherEndpointsAreHidden(String endpoint) {
    assertEquals(HttpStatus.NOT_FOUND,
        http.getForEntity("/actuator/" + endpoint, String.class).getStatusCode());
  }

  @Test
  void openApiListsBothOperations() {
    String spec = http.getForObject("/v3/api-docs", String.class);
    assertTrue(spec.contains("/api/v1/onboardings"));
    assertTrue(spec.contains("/api/v1/onboardings/{id}"));
    assertTrue(spec.contains("\"409\""));
    assertTrue(spec.contains("\"404\""));
    assertFalse(spec.contains("OnboardingEntity"), "the JPA entity is not a schema");
  }
}
