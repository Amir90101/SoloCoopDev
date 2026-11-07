package com.css.onboarding.domain;

/** Unchecked, no Spring: the web advice turns it into 404. */
public class OnboardingNotFound extends RuntimeException {
  public OnboardingNotFound(OnboardingId id) {
    super("onboarding not found: " + id.value());
  }
}
