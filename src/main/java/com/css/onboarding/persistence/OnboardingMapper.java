package com.css.onboarding.persistence;

import com.css.onboarding.domain.Onboarding;
import com.css.onboarding.domain.OnboardingId;
import com.css.onboarding.domain.OnboardingStatus;
import java.util.UUID;

/** Sealed status in, string column out. Translates only; the policy decides moves. */
public final class OnboardingMapper {

  private OnboardingMapper() {
  }

  public static OnboardingEntity toEntity(UUID id, String businessKey, OnboardingStatus status, String title) {
    return new OnboardingEntity(id, businessKey, toColumn(status), title);
  }

  public static Onboarding toDomain(OnboardingEntity row) {
    return new Onboarding(
        new OnboardingId(row.getId()), row.getBusinessKey(), toStatus(row.getStatus()), row.getTitle());
  }

  public static String toColumn(OnboardingStatus status) {
    return switch (status) {
      case OnboardingStatus.Planned p -> "PLANNED";
      case OnboardingStatus.Started s -> "STARTED";
      case OnboardingStatus.Done d -> "DONE";
    };
  }

  /** Text outside the CHECK is a bug, not a new status. */
  public static OnboardingStatus toStatus(String column) {
    return switch (column) {
      case "PLANNED" -> new OnboardingStatus.Planned();
      case "STARTED" -> new OnboardingStatus.Started();
      case "DONE" -> new OnboardingStatus.Done();
      case null, default -> throw new IllegalStateException("unknown status in row: " + column);
    };
  }
}
