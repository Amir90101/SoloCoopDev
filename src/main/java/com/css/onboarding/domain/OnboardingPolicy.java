package com.css.onboarding.domain;

import java.util.List;

public final class OnboardingPolicy {

  private final Rule rule;

  public OnboardingPolicy(Rule rule) {
    this.rule = rule;
  }

  public OnboardingPolicy() {
    this(defaultRule());
  }

  public static Rule defaultRule() {
    return new ChainRule(List.of(new NotStartedCannotFinish(), new TransitionRule()));
  }

  public OnboardingStatus move(OnboardingStatus from, OnboardingStatus to) {
    rule.check(from, to);
    return to;
  }
}
