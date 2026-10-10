package com.css.onboarding.domain;

import java.util.UUID;

public record OnboardingId(UUID value) {

  public OnboardingId {
    if (value == null) {
      throw new IllegalArgumentException("onboarding id must not be null");
    }
  }

  public static OnboardingId newId() {
    return new OnboardingId(UUID.randomUUID());
  }

  public static OnboardingId parse(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("onboarding id must not be blank");
    }
    return new OnboardingId(UUID.fromString(raw.trim()));
  }
}
