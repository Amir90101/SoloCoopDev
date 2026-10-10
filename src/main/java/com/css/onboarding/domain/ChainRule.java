package com.css.onboarding.domain;

import java.util.List;

/** Runs rules in order. The first one that throws stops the move. */
public final class ChainRule implements Rule {

  private final List<Rule> rules;

  public ChainRule(List<Rule> rules) {
    this.rules = List.copyOf(rules);
  }

  @Override
  public void check(OnboardingStatus from, OnboardingStatus to) {
    for (Rule rule : rules) {
      rule.check(from, to);
    }
  }
}
