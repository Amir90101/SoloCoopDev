package com.css.onboarding.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.css.onboarding")
// Application lives in config; entities live in persistence.
@EntityScan(basePackages = "com.css.onboarding.persistence")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
