package com.css.onboarding.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class OnboardingIdTest {

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void nullOrBlankThrows(String raw) {
    assertThrows(IllegalArgumentException.class, () -> OnboardingId.parse(raw));
  }

  @Test
  void garbageThrows() {
    assertThrows(IllegalArgumentException.class, () -> OnboardingId.parse("OB-7"));
  }
}
