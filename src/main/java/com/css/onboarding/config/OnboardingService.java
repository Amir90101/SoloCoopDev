package com.css.onboarding.config;

import com.css.onboarding.domain.CompletionNotifier;
import com.css.onboarding.domain.DuplicateOnboarding;
import com.css.onboarding.domain.OnboardingId;
import com.css.onboarding.domain.OnboardingStatus;
import com.css.onboarding.domain.Rule;
import com.css.onboarding.persistence.OnboardingJdbc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {

  private final Rule rules;
  private final OnboardingJdbc onboardings;
  private final CompletionNotifier notifier;

  public OnboardingService(Rule rules, OnboardingJdbc onboardings, CompletionNotifier notifier) {
    this.rules = rules;
    this.onboardings = onboardings;
    this.notifier = notifier;
  }

  public OnboardingStatus move(String businessKey, OnboardingStatus from, OnboardingStatus to) {
    rules.check(from, to);
    if (to instanceof OnboardingStatus.Done) {
      notifier.completed(businessKey);
    }
    return to;
  }

  /** One transaction per call. */
  @Transactional
  public void register(OnboardingId id, String businessKey, String title) {
    requireKey(businessKey);
    try {
      onboardings.insert(id.value(), businessKey, new OnboardingStatus.Planned(), title);
    } catch (DataIntegrityViolationException ex) {
      throw new DuplicateOnboarding(businessKey);
    }
  }

  /** One transaction, two inserts. The second one fails and takes the first with it. */
  @Transactional
  public void insertTwice(String businessKey) {
    requireKey(businessKey);
    onboardings.insert(OnboardingId.newId().value(), businessKey, new OnboardingStatus.Planned(), "first");
    try {
      onboardings.insert(OnboardingId.newId().value(), businessKey, new OnboardingStatus.Planned(), "second");
    } catch (DataIntegrityViolationException ex) {
      throw new DuplicateOnboarding(businessKey);
    }
  }

  private static void requireKey(String businessKey) {
    if (businessKey == null || businessKey.isBlank()) {
      throw new IllegalArgumentException("business key must not be blank");
    }
  }
}
