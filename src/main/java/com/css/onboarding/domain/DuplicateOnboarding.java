package com.css.onboarding.domain;

/** Unchecked on purpose: Spring rolls back when it leaves a @Transactional method. */
public class DuplicateOnboarding extends RuntimeException {
  public DuplicateOnboarding(String businessKey) {
    super("duplicate onboarding: " + businessKey);
  }
}
