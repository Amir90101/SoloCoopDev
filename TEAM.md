# Team split

Two people, two roles. Each commits under their own name.

## Role A — domain and tests

Owns `domain/`, `src/test/.../domain/`, README sections Product / Core item / Status table / Forbidden.

- Lab 1: `OnboardingId`, `OnboardingStatus`, `OnboardingPolicy` (first as a plain `switch`), `OnboardingPolicyTest`, `OnboardingIdTest`, README.
- Lab 2: extract `Rule`, `TransitionRule`, `NotStartedCannotFinish`, `ChainRule`. No Spring imports.
- Lab 3 week 4: four-row `@CsvSource`, mocked port test (`OnboardingServiceTest`), `DomainPurityTest`.

## Role B — Spring, SQL, JDBC

Owns `config/`, `client/`, `persistence/`, `db/schema.sql`, `pom.xml`, `docker-compose.yml`, `OnboardingPersistenceTest`.

- Lab 2: parent POM, `Application`, `RuleConfig` (`@Bean Rule`), `OnboardingService`, package diagram, PR.
- Lab 3 week 5: table by hand, rollback proved in psql, `DuplicateOnboarding` handling, two count tests.
- Lab 3 week 6: `schema.sql` file, `OnboardingJdbc` with `?` placeholders.

## Contract between the roles

- `Rule.check(OnboardingStatus from, OnboardingStatus to)` throws `IllegalStateException`.
- `OnboardingStatus.code()` / `fromCode(String)` give the text stored in the `status` column.
- `CompletionNotifier.completed(String businessKey)` is the outbound port.
- `DuplicateOnboarding` is unchecked and lives in `domain`.

## Branches and PRs (replace CSS-XX with your Jira IDs)

| Lab | Author | Branch | PR title |
| --- | --- | --- | --- |
| 1 | A | `lab1-domain` | `CSS-XX statuses and policy` |
| 2 | B | `lab2-spring` | `CSS-XX join Spring Boot` |
| 3 | B | `lab3-sql-jdbc` | `CSS-XX schema and JDBC` |

Each PR is reviewed by the other person. Role A also owns the week 4 tests inside the Lab 3 PR (separate commits).
