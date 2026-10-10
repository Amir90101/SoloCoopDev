package com.css.onboarding.domain;

import com.css.onboarding.domain.OnboardingStatus.Done;
import com.css.onboarding.domain.OnboardingStatus.Started;
import java.util.Objects;

/** Stop-factor: an onboarding cannot be finished before it has started. */
public final class NotStartedCannotFinish implements Rule {

  @Override
  public void check(OnboardingStatus from, OnboardingStatus to) {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");
    if (to instanceof Done && !(from instanceof Started)) {
      throw new IllegalStateException(
          "cannot finish an onboarding that was not started (from " + from.code() + ")");
    }
  }
}
