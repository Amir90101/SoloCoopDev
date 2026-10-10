package com.css.onboarding.domain;

public sealed interface OnboardingStatus {

  String code();

  record Planned() implements OnboardingStatus {
    public String code() { return "PLANNED"; }
  }

  record Started() implements OnboardingStatus {
    public String code() { return "STARTED"; }
  }

  record Done() implements OnboardingStatus {
    public String code() { return "DONE"; }
  }

  static OnboardingStatus fromCode(String code) {
    if (code == null) {
      throw new IllegalArgumentException("status code is null");
    }
    return switch (code) {
      case "PLANNED" -> new Planned();
      case "STARTED" -> new Started();
      case "DONE" -> new Done();
      default -> throw new IllegalArgumentException("unknown status: " + code);
    };
  }
}
