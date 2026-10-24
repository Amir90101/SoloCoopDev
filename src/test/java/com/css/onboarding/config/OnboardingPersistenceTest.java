package com.css.onboarding.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.css.onboarding.domain.DuplicateOnboarding;
import com.css.onboarding.domain.OnboardingId;
import com.css.onboarding.domain.OnboardingStatus;
import com.css.onboarding.persistence.OnboardingJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

// Flyway runs V1 when the context starts, then Hibernate validates.
// No @Transactional here: the count must come from committed data.
@SpringBootTest(classes = Application.class)
class OnboardingPersistenceTest {

  @Autowired JdbcTemplate jdbc;
  @Autowired OnboardingService service;
  @Autowired OnboardingJpa onboardings;

  @BeforeEach
  void empty() {
    // Flyway owns the schema. Delete rows, never DROP the table.
    jdbc.update("DELETE FROM onboarding");
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
