package com.css.onboarding.domain;

/** What the service hands out. Not the entity, not the JSON. */
public record Onboarding(OnboardingId id, String businessKey, OnboardingStatus status, String title) {
}
