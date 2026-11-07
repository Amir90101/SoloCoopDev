package com.css.onboarding.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/** The row of V1__create_onboarding.sql. No @GeneratedValue: the id is OnboardingId. */
@Entity
@Table(name = "onboarding")
public class OnboardingEntity {

  @Id
  private UUID id;

  @Column(name = "business_key", nullable = false, unique = true)
  private String businessKey;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private String title;

  @Column(name = "created_at", insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  protected OnboardingEntity() {
  }

  public OnboardingEntity(UUID id, String businessKey, String status, String title) {
    this.id = id;
    this.businessKey = businessKey;
    this.status = status;
    this.title = title;
  }

  public UUID getId() { return id; }

  public String getBusinessKey() { return businessKey; }

  public String getStatus() { return status; }

  public String getTitle() { return title; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
}
