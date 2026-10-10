package com.css.onboarding.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainPurityTest {

  @Test
  void domainImportsNoSpringAndNoJdbc() throws IOException {
    Path dir = Path.of("src/main/java/com/css/onboarding/domain");
    try (var files = Files.list(dir)) {
      List<Path> sources = files.filter(p -> p.toString().endsWith(".java")).toList();
      for (Path p : sources) {
        String text = Files.readString(p);
        assertFalse(text.contains("org.springframework"), p + " imports Spring");
        assertFalse(text.contains("java.sql"), p + " imports java.sql");
      }
    }
  }
}
