package com.css.onboarding.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OnboardingPolicyTest {

  private final OnboardingPolicy policy = new OnboardingPolicy();

  @ParameterizedTest(name = "{0} -> {1} allowed={2}")
  @CsvSource({
      "PLANNED,STARTED,true",
      "STARTED,DONE,true",
      "PLANNED,DONE,false",
      "DONE,PLANNED,false"
  })
  void fourReadmeRows(String from, String to, boolean allowed) {
    OnboardingStatus f = OnboardingStatus.fromCode(from);
    OnboardingStatus t = OnboardingStatus.fromCode(to);
    if (allowed) {
      assertEquals(t, policy.move(f, t));
    } else {
      assertThrows(IllegalStateException.class, () -> policy.move(f, t));
    }
  }
}
