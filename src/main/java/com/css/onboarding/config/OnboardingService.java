package com.css.onboarding.config;

import static com.css.onboarding.persistence.OnboardingMapper.toEntity;

import com.css.onboarding.domain.CompletionNotifier;
import com.css.onboarding.domain.DuplicateOnboarding;
import com.css.onboarding.domain.OnboardingId;
import com.css.onboarding.domain.OnboardingStatus;
import com.css.onboarding.domain.Rule;
import com.css.onboarding.persistence.OnboardingEntity;
import com.css.onboarding.persistence.OnboardingJpa;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {

  private final Rule rules;
  private final OnboardingJpa onboardings;
  private final CompletionNotifier notifier;

  public OnboardingService(Rule rules, OnboardingJpa onboardings, CompletionNotifier notifier) {
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
      onboardings.insert(toEntity(id.value(), businessKey, new OnboardingStatus.Planned(), title));
      onboardings.flush();
    } catch (DataIntegrityViolationException ex) {
      throw new DuplicateOnboarding(businessKey);
    }
  }

  /** One transaction, two inserts. The second one fails and takes the first with it. */
  @Transactional
  public void insertTwice(String businessKey) {
    requireKey(businessKey);
    onboardings.insert(newRow(businessKey, "first"));
    try {
      onboardings.insert(newRow(businessKey, "second")); // a second UUID, the same key
      onboardings.flush();
    } catch (DataIntegrityViolationException ex) {
      throw new DuplicateOnboarding(businessKey);
    }
  }

  private static OnboardingEntity newRow(String businessKey, String title) {
    return toEntity(OnboardingId.newId().value(), businessKey, new OnboardingStatus.Planned(), title);
  }

  private static void requireKey(String businessKey) {
    if (businessKey == null || businessKey.isBlank()) {
      throw new IllegalArgumentException("business key must not be blank");
    }
  }
}
