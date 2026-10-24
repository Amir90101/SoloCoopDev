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
        OnboardingId  OnboardingStatus  OnboardingPolicy   OnboardingEntity (@Entity)
        Rule: TransitionRule, NotStartedCannotFinish, ChainRule   OnboardingJpa (EntityManager)
        CompletionNotifier (port)  DuplicateOnboarding           OnboardingMapper
                     (no Spring, no java.sql, no jakarta.persistence)
```

Arrows point inward. `domain` imports neither `org.springframework`, `java.sql` nor `jakarta.persistence`.

## Schema

Flyway owns the schema: `src/main/resources/db/migration/V1__create_onboarding.sql` runs on startup, then Hibernate checks the entity against it (`ddl-auto: validate`). Hibernate never creates or alters tables.

- `onboarding.status` CHECK lists `PLANNED`, `STARTED`, `DONE` — the same tokens as `OnboardingStatus` and the status table above.
- A schema change is a new file (`V2__...sql`). A migration that already ran is never edited.
- Tests `DELETE FROM onboarding`; they never drop the table.

Reset a local database that was created by hand (connect to `postgres`, not `css`):

```
DROP DATABASE IF EXISTS css WITH (FORCE);
CREATE DATABASE css OWNER css;
```

## Run

```
docker compose up -d     # Postgres only: db css, user css, password css
mvn -q verify
mvn spring-boot:run
```

## Team

See `TEAM.md`.
