package com.css.onboarding.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.css.onboarding.client.DownstreamProperties;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** The downstream values come from application-dev.yaml. No socket is opened to 9090. */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("dev")
class DevProfileTest {

  @Autowired DownstreamProperties downstream;

  @Test
  void downstreamIsBoundFromYaml() {
    assertEquals(URI.create("http://localhost:9090"), downstream.baseUrl());
    assertEquals(Duration.ofSeconds(2), downstream.timeout());
  }
}
