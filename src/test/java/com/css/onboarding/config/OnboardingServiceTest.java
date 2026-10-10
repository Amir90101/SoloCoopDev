package com.css.onboarding.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.css.onboarding.domain.CompletionNotifier;
import com.css.onboarding.domain.OnboardingPolicy;
import com.css.onboarding.domain.OnboardingStatus.Done;
import com.css.onboarding.domain.OnboardingStatus.Planned;
import com.css.onboarding.domain.OnboardingStatus.Started;
import com.css.onboarding.persistence.OnboardingJdbc;
import org.junit.jupiter.api.Test;

/** Outbound port is mocked. No HTTP, no database. */
class OnboardingServiceTest {

  private final CompletionNotifier notifier = mock(CompletionNotifier.class);
  private final OnboardingService service =
      new OnboardingService(OnboardingPolicy.defaultRule(), mock(OnboardingJdbc.class), notifier);

  @Test
  void finishingNotifiesOnce() {
    service.move("OB-7", new Started(), new Done());
    verify(notifier).completed("OB-7");
  }

  @Test
  void forbiddenMoveDoesNotNotify() {
    assertThrows(IllegalStateException.class,
        () -> service.move("OB-7", new Planned(), new Done()));
    verify(notifier, never()).completed("OB-7");
  }
}
