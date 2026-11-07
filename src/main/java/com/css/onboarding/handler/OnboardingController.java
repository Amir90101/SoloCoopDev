package com.css.onboarding.handler;

import com.css.onboarding.config.OnboardingService;
import com.css.onboarding.domain.Onboarding;
import com.css.onboarding.domain.OnboardingId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** HTTP adapter. Chooses the status; the service still inserts. */
@RestController
@RequestMapping(path = "/api/v1/onboardings", produces = MediaType.APPLICATION_JSON_VALUE)
public class OnboardingController {

  private final OnboardingService service;

  public OnboardingController(OnboardingService service) {
    this.service = service;
  }

  @Operation(summary = "Create one onboarding in status PLANNED")
  @ApiResponse(responseCode = "201", description = "Created; Location points to the GET")
  @ApiResponse(responseCode = "400", description = "VALIDATION",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "DUPLICATE business key",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<OnboardingResponse> create(@Valid @RequestBody CreateOnboardingRequest body) {
    Onboarding created = service.register(OnboardingId.newId(), body.businessKey(), body.title());
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(created.id().value())
        .toUri();
    return ResponseEntity.created(location).body(OnboardingResponse.from(created));
  }

  @Operation(summary = "Read one onboarding by its UUID")
  @ApiResponse(responseCode = "200", description = "Found")
  @ApiResponse(responseCode = "400", description = "VALIDATION: id is not a UUID",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "NOT_FOUND",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/{id}")
  public OnboardingResponse get(@PathVariable UUID id) {
    return OnboardingResponse.from(service.get(new OnboardingId(id)));
  }
}
