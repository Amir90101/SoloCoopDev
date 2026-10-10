package com.css.onboarding.domain;

import com.css.onboarding.domain.OnboardingStatus.Started;
import com.css.onboarding.domain.OnboardingStatus.Planned;
import com.css.onboarding.domain.OnboardingStatus.Done;
import java.util.Objects;

/** The status table: Planned -> Started -> Done. Everything else is forbidden. */
public final class TransitionRule implements Rule {

  @Override
  public void check(OnboardingStatus from, OnboardingStatus to) {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");
    boolean allowed =
        (from instanceof Planned && to instanceof Started)
            || (from instanceof Started && to instanceof Done);
    if (!allowed) {
      throw new IllegalStateException(
          "move not allowed: " + from.code() + " -> " + to.code());
    }
  }
}
