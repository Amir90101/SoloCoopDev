package com.css.onboarding.config;

import com.css.onboarding.client.DownstreamProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.css.onboarding")
// Application lives in config; entities live in persistence.
@EntityScan(basePackages = "com.css.onboarding.persistence")
@EnableConfigurationProperties(DownstreamProperties.class)
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
