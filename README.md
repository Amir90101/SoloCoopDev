# Onboarding: статусы адаптации нового сотрудника

## Product

We build an internal onboarding tool. People track **onboardings** (one per new hire).

## Core item

An onboarding has a surrogate `OnboardingId` (UUID), a human business key (`OB-7`), a title and an `OnboardingStatus`.

- `OnboardingId` — UUID, `parse` rejects null and blank.
- `OnboardingStatus` — sealed: `Planned`, `Started`, `Done`.
- `OnboardingPolicy` — `move(from, to)` returns the new status or throws `IllegalStateException`.

## Status table

| # | From | To | Result | Reason |
| --- | --- | --- | --- | --- |
| 1 | Planned | Started | allowed | the new hire's first day came |
| 2 | Started | Done | allowed | the checklist is complete and HR signed off |
| 3 | Planned | Done | forbidden | skipped the whole checklist |
| 4 | Done | Planned | forbidden | reopen after HR sign-off |

## Forbidden — why

- **Planned → Done.** The hire would count as onboarded without any training or access setup. `NotStartedCannotFinish` stops it.
- **Done → Planned.** HR already signed off. Reopening needs a new onboarding, not a status reset.

## Packages

```
  dto          client                   handler         config
  (JSON later) LoggingCompletionNotifier (HTTP week 9)  Application
                                                        RuleConfig
                                                        OnboardingService (@Service)
       \            \                     /                |
        \            \                   /           injects Rule
         \            \                 /
                     domain                          persistence
        OnboardingId  OnboardingStatus  OnboardingPolicy   OnboardingJdbc (JDBC)
        Rule: TransitionRule, NotStartedCannotFinish, ChainRule
        CompletionNotifier (port)  DuplicateOnboarding
                     (no Spring, no java.sql)
```

Arrows point inward. `domain` imports neither `org.springframework` nor `java.sql`.

## Run

```
docker compose up -d     # Postgres only: db css, user css, password css
mvn -q verify
mvn spring-boot:run
```

## Team

See `TEAM.md`.
