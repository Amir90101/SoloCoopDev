package com.css.onboarding.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.css.onboarding.domain.DuplicateOnboarding;
import com.css.onboarding.domain.OnboardingId;
import com.css.onboarding.domain.OnboardingStatus;
import com.css.onboarding.persistence.OnboardingJdbc;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

// No @Transactional here: the count must come from committed data.
@SpringBootTest(classes = Application.class)
class OnboardingPersistenceTest {

  @Autowired JdbcTemplate jdbc;
  @Autowired OnboardingService service;
  @Autowired OnboardingJdbc onboardings;

  @BeforeEach
  void cleanDatabase() throws IOException {
    jdbc.execute("DROP TABLE IF EXISTS onboarding");
    jdbc.execute(Files.readString(Path.of("src/main/resources/db/schema.sql")));
  }

  @Test
  void secondStatementRollsBack() {
    assertThrows(DuplicateOnboarding.class, () -> service.insertTwice("OB-7"));
    assertEquals(0, onboardings.count("OB-7"));
  }

  @Test
  void secondOnboardingKeepsTheFirst() {
    service.register(OnboardingId.newId(), "OB-7", "New hire Alex");
    assertThrows(DuplicateOnboarding.class,
        () -> service.register(OnboardingId.newId(), "OB-7", "Again"));
    assertEquals(1, onboardings.count("OB-7"));
  }

  @Test
  void statusRoundTrips() {
    service.register(OnboardingId.newId(), "OB-8", "New hire Dana");
    assertEquals(new OnboardingStatus.Planned(), onboardings.findStatus("OB-8"));
  }

  @Test
  void checkConstraintRejectsUnknownStatus() {
    assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
        "INSERT INTO onboarding (id, business_key, status, title) VALUES (?, ?, ?, ?)",
        OnboardingId.newId().value(), "OB-9", "PAID", "Bad row"));
  }
}
