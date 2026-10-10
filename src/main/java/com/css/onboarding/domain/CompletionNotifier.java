package com.css.onboarding.domain;

/** Outbound port. The real adapter lives in client; tests mock it. */
public interface CompletionNotifier {
  void completed(String businessKey);
}
