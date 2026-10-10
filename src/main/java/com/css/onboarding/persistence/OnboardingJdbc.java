package com.css.onboarding.persistence;

import com.css.onboarding.domain.OnboardingStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OnboardingJdbc {

  private final JdbcTemplate jdbc;

  public OnboardingJdbc(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void insert(UUID id, String businessKey, OnboardingStatus status, String title) {
    jdbc.update("""
        INSERT INTO onboarding (id, business_key, status, title)
        VALUES (?, ?, ?, ?)
        """, id, businessKey, status.code(), title);
  }

  public int count(String businessKey) {
    Integer n = jdbc.queryForObject(
        "SELECT count(*) FROM onboarding WHERE business_key = ?",
        Integer.class,
        businessKey);
    return n == null ? 0 : n;
  }

  /** Unknown text in the column throws; it never becomes a new status. */
  public OnboardingStatus findStatus(String businessKey) {
    List<String> codes = jdbc.queryForList(
        "SELECT status FROM onboarding WHERE business_key = ?",
        String.class,
        businessKey);
    return codes.isEmpty() ? null : OnboardingStatus.fromCode(codes.get(0));
  }
}
