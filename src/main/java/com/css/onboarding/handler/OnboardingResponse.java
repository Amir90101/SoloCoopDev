package com.css.onboarding.handler;

import com.css.onboarding.domain.Onboarding;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** What goes on the wire. The JPA entity never does. */
public record OnboardingResponse(
    UUID id,
    @Schema(example = "OB-7") String businessKey,
    @Schema(example = "PLANNED", allowableValues = {"PLANNED", "STARTED", "DONE"}) String status,
    @Schema(example = "New hire Alex") String title) {

  static OnboardingResponse from(Onboarding onboarding) {
    return new OnboardingResponse(
        onboarding.id().value(),
        onboarding.businessKey(),
        onboarding.status().code(),
        onboarding.title());
  }
}
