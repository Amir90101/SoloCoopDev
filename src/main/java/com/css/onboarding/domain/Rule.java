package com.css.onboarding.domain;

/** Throws IllegalStateException when the move is forbidden. */
public interface Rule {
  void check(OnboardingStatus from, OnboardingStatus to);
}
