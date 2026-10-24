package com.css.onboarding.persistence;

import com.css.onboarding.domain.OnboardingStatus;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;

/** persist always inserts. Spring Data save() with our UUID would SELECT first. */
@Repository
public class OnboardingJpa {

  private final EntityManager em;

  public OnboardingJpa(EntityManager em) {
    this.em = em;
  }

  public void insert(OnboardingEntity onboarding) {
    em.persist(onboarding);
  }

  /** persist only remembers the row; flush sends the INSERT so the UNIQUE violation surfaces here. */
  public void flush() {
    em.flush();
  }

  public int count(String businessKey) {
    Long n = em.createQuery("""
            select count(o) from OnboardingEntity o
            where o.businessKey = :key
            """, Long.class)
        .setParameter("key", businessKey)
        .getSingleResult();
    return n.intValue();
  }

  public OnboardingStatus findStatus(String businessKey) {
    List<String> codes = em.createQuery("""
            select o.status from OnboardingEntity o
            where o.businessKey = :key
            """, String.class)
        .setParameter("key", businessKey)
        .getResultList();
    return codes.isEmpty() ? null : OnboardingMapper.toStatus(codes.get(0));
  }
}
