package com.css.onboarding.config;

import com.css.onboarding.domain.OnboardingPolicy;
import com.css.onboarding.domain.Rule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleConfig {

  @Bean
  public Rule rule() {
    return OnboardingPolicy.defaultRule();
  }
}
