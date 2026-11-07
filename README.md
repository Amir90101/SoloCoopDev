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
  dto          client                     handler                  config
  (unused)     LoggingCompletionNotifier  OnboardingController     Application
               DownstreamProperties       request/response/error   
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

`handler` holds the JSON records and the one advice that picks 400/404/409/415/500.

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

## REST API (v1)

| Call | Result |
| --- | --- |
| `POST /api/v1/onboardings` `{"businessKey":"OB-7","title":"New hire Alex"}` | **201**, `Location: /api/v1/onboardings/{uuid}`, body `id, businessKey, status, title` |
| title or business key blank, title > 120 chars, body not JSON | **400** `VALIDATION` (message names the field: `title must not be blank`) |
| `GET /api/v1/onboardings/OB-7` (not a UUID) | **400** `VALIDATION`, `id must be a UUID` |
| `GET /api/v1/onboardings/{uuid}` missing | **404** `NOT_FOUND` |
| same business key again | **409** `DUPLICATE`; the first row stays (count is 1) |
| wrong `Content-Type` | **415** `UNSUPPORTED` |
| anything unmapped | **500** `INTERNAL`, `unexpected error` (details only in the log) |

Every error body is exactly `{"code", "message", "correlationId"}`. `X-Correlation-Id` from the request is echoed; if missing, a new one is minted. The id in the path is the UUID; the business key stays in the body.

Contract: `/swagger-ui.html`, spec at `/v3/api-docs`.

Compatibility: an optional new field stays on `/api/v1`. Renaming `title`, dropping `Location`, or changing a status token means `/api/v2`.

## Config and profiles

| File | Holds |
| --- | --- |
| `application.yaml` | shared: `ddl-auto: validate`, Flyway, error-leak off, actuator exposure, `spring.profiles.default: dev` |
| `application-dev.yaml` | laptop: `localhost:5432/css`, `css`/`css`, `app.downstream.base-url: http://localhost:9090`, `timeout: 2s` |
| `application-prod.yaml` | `CSS_DB_URL`, `CSS_DB_USER`, `CSS_DB_PASSWORD`, `CSS_DOWNSTREAM_URL` from the environment, no defaults |

`app.downstream` is bound by `DownstreamProperties` (`@ConfigurationProperties`). No URL literal lives in `src/main/java`. Production runs with `SPRING_PROFILES_ACTIVE=prod`.

Actuator exposes only `health` (no details) and `info`. `env`, `beans`, `heapdump`, `shutdown` are 404.

## Run

```
docker compose up -d     # Postgres only: db css, user css, password css
mvn -q verify
mvn spring-boot:run
curl -i localhost:8080/actuator/health
curl -i -H 'Content-Type: application/json' -d '{"businessKey":"OB-7","title":"New hire Alex"}' localhost:8080/api/v1/onboardings
```

## Team

See `TEAM.md`.
