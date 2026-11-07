package com.css.onboarding.handler;

import io.swagger.v3.oas.annotations.media.Schema;

/** Every failure body. No stack trace, no exception class name. */
public record ErrorResponse(
    @Schema(example = "DUPLICATE") String code,
    @Schema(example = "duplicate onboarding: OB-7") String message,
    @Schema(example = "3f2b8c1e-6d1a-4c55-9a1e-0c8a3f1b2d77") String correlationId) {
}
