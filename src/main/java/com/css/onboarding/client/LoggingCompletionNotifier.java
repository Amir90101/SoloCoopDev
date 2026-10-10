package com.css.onboarding.client;

import com.css.onboarding.domain.CompletionNotifier;
import org.springframework.stereotype.Component;

/** Stand-in for a real HTTP call (week 9). */
@Component
public class LoggingCompletionNotifier implements CompletionNotifier {

  private static final System.Logger LOG = System.getLogger("notifier");

  @Override
  public void completed(String businessKey) {
    LOG.log(System.Logger.Level.INFO, "onboarding completed: {0}", businessKey);
  }
}
