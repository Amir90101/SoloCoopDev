package com.css.onboarding.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** POST body. Validation lives here, not in domain. */
public record CreateOnboardingRequest(
    @Schema(example = "OB-7", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String businessKey,
    @Schema(example = "New hire Alex", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Size(max = 120) String title) {
}
