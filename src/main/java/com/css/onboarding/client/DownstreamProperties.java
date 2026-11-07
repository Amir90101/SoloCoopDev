package com.css.onboarding.client;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** app.downstream.* from yaml. Lab 6 makes the call; for now the values are only bound. */
@ConfigurationProperties(prefix = "app.downstream")
public record DownstreamProperties(URI baseUrl, Duration timeout) {
}
