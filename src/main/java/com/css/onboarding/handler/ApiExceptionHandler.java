package com.css.onboarding.handler;

import com.css.onboarding.domain.DuplicateOnboarding;
import com.css.onboarding.domain.OnboardingNotFound;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** The only place that knows 400, 404, 409, 415 and 500. */
@RestControllerAdvice
public class ApiExceptionHandler {

  static final String CORRELATION_HEADER = "X-Correlation-Id";

  private static final System.Logger LOG = System.getLogger("api");

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> invalidBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .sorted(Comparator.comparing(FieldError::getField))
        .map(e -> words(e.getField()) + " " + e.getDefaultMessage())
        .collect(Collectors.joining("; "));
    return error(HttpStatus.BAD_REQUEST, "VALIDATION", message, req);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ResponseEntity<ErrorResponse> badPathVariable(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    return error(HttpStatus.BAD_REQUEST, "VALIDATION", ex.getName() + " must be a UUID", req);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ErrorResponse> notJson(HttpServletRequest req) {
    return error(HttpStatus.BAD_REQUEST, "VALIDATION", "body must be JSON", req);
  }

  @ExceptionHandler(OnboardingNotFound.class)
  ResponseEntity<ErrorResponse> notFound(OnboardingNotFound ex, HttpServletRequest req) {
    return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req);
  }

  /** Unknown path, including actuator endpoints that are not exposed. */
  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<ErrorResponse> noRoute(HttpServletRequest req) {
    return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "no such resource", req);
  }

  @ExceptionHandler(DuplicateOnboarding.class)
  ResponseEntity<ErrorResponse> duplicate(DuplicateOnboarding ex, HttpServletRequest req) {
    return error(HttpStatus.CONFLICT, "DUPLICATE", ex.getMessage(), req);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  ResponseEntity<ErrorResponse> unsupported(HttpServletRequest req) {
    return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED", "content type must be application/json", req);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ErrorResponse> wrongMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
    return error(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMethod() + " is not supported", req);
  }

  /** A bug we did not map. The text goes to the log, never to the body. */
  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> unexpected(Exception ex, HttpServletRequest req) {
    String correlationId = correlationId(req);
    LOG.log(System.Logger.Level.ERROR, "unexpected error, correlationId=" + correlationId, ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .header(CORRELATION_HEADER, correlationId)
        .body(new ErrorResponse("INTERNAL", "unexpected error", correlationId));
  }

  private static ResponseEntity<ErrorResponse> error(
      HttpStatus status, String code, String message, HttpServletRequest req) {
    String correlationId = correlationId(req);
    return ResponseEntity.status(status)
        .header(CORRELATION_HEADER, correlationId)
        .body(new ErrorResponse(code, message, correlationId));
  }

  /** Repeat the caller's id; mint one if it is missing or blank. */
  private static String correlationId(HttpServletRequest req) {
    String sent = req.getHeader(CORRELATION_HEADER);
    return sent == null || sent.isBlank() ? UUID.randomUUID().toString() : sent.trim();
  }

  /** businessKey -> "business key", so the message reads like the README. */
  private static String words(String field) {
    return field.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
  }
}
