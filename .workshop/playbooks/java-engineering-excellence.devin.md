# Java Engineering Excellence

## Overview

A compound playbook that drives **four functional engineering tasks** against a
Java/Spring Boot codebase — Feature Development, Issue Triage (security
remediation), App Modernization (major version upgrade), and Test Generation —
each gated by a **programmatic verification loop** (build + test + coverage).

The playbook is designed to be invoked as `!java-engineering-excellence` with a
task-type selector. Each task type can run independently or as part of a
sequenced demo showing the full breadth of Devin's engineering capabilities on a
single repo.

## Guiding Principle

**Every change must pass the verification gate before it is trusted.** The gate
is `./gradlew clean test spotlessCheck` (formatting + unit/integration tests).
For coverage tasks the gate extends to
`./gradlew jacocoTestCoverageVerification`. A PR whose verification is not green
is not done — regardless of how reasonable the code looks.

## Required from user

- **Repository**: a Java/Spring Boot Gradle project with an existing test suite
  and JaCoCo coverage configured.
- **Task type**: one of `feature-dev`, `issue-triage`, `app-modernization`, or
  `test-generation`.
- **Task details** (varies by type):
  - `feature-dev`: feature name, entity/endpoint description, acceptance criteria
  - `issue-triage`: the issue, bug report, or CVE identifier to remediate
  - `app-modernization`: source version → target version (e.g., Java 11 → 21,
    Spring Boot 2.6 → 3.5)
  - `test-generation`: target coverage threshold or specific untested classes

## Procedure

### Common preamble (all task types)

1. Clone and build the project: `./gradlew clean build -x test` to confirm
   compilation succeeds on the baseline.
2. Run the full verification gate: `./gradlew clean test spotlessCheck`. Record
   the baseline state (pass count, coverage %).
3. Create a feature branch: `devin/<timestamp>-<task-type>-<short-desc>`.

### Task: `feature-dev`

4. Analyze existing domain model, MyBatis mappers, and API controllers to
   understand conventions (package structure, naming, DTO patterns).
5. Implement the feature following existing patterns:
   - Domain entity in `core/`
   - MyBatis mapper interface + XML in `infrastructure/mybatis/mapper/`
   - Flyway migration in `src/main/resources/db/migration/`
   - Service interface in `core/service/`, implementation in `infrastructure/`
   - REST controller in `api/`, GraphQL datafetcher in `graphql/`
   - Unit tests for service + API (mirror existing test style)
6. Run verification gate. Fix any failures.
7. Run `./gradlew spotlessApply` to auto-format, then commit.
8. Open PR with test results included.

### Task: `issue-triage`

4. Reproduce or identify the issue:
   - For bugs: write a failing test that demonstrates the defect.
   - For CVEs: run `./gradlew dependencyInsight --dependency <artifact>` to
     confirm the vulnerable version is present.
5. Implement the fix:
   - For bugs: fix the root cause, confirm the failing test now passes.
   - For CVEs: bump the dependency version in `build.gradle`, verify no API
     breaks, run full test suite.
6. Run verification gate. Fix any regressions.
7. Open PR with before/after evidence (CVE scan output or test failure→pass).

### Task: `app-modernization`

4. Audit the dependency tree for version-locked artifacts:
   - Spring Boot parent version
   - Java source/target compatibility
   - Third-party libraries with major-version jumps (MyBatis, DGS, jjwt, etc.)
5. Upgrade in layers (each layer verified independently):
   - **Build config**: Gradle plugins, Java version, Spring Boot version
   - **Namespace migration**: `javax.*` → `jakarta.*` across all source files
   - **Security config**: `WebSecurityConfigurerAdapter` → `SecurityFilterChain`
     bean with lambda DSL
   - **Infrastructure**: MyBatis starter, Flyway, database driver compatibility
   - **GraphQL**: DGS framework version bump + API changes
   - **Date/time**: `joda-time` → `java.time` (if targeted)
6. After each layer, run the verification gate. The test suite is the contract —
   if tests break, the upgrade introduced a regression. Fix before proceeding.
7. Run `./gradlew spotlessApply`, commit per-layer for reviewability.
8. Open PR with the full upgrade changelog and verification report.

### Task: `test-generation`

4. Run `./gradlew jacocoTestReport` and identify classes below threshold.
5. For each uncovered class:
   - Read the implementation to understand behavior and edge cases.
   - Write unit tests following existing test patterns (JUnit 5 +
     REST-Assured MockMvc for controllers, Mockito for services).
   - Verify each new test passes individually.
6. Run full verification gate including
   `./gradlew jacocoTestCoverageVerification`.
7. Open PR with coverage diff (before → after).

## Specifications (postconditions)

- Every PR passes `./gradlew clean test spotlessCheck` in CI.
- App-modernization PRs compile and run on the target Java version.
- Feature-dev PRs include tests that cover the new code paths.
- Issue-triage PRs include a regression test for the fixed bug/CVE.
- Test-generation PRs do not modify production code.
- No secrets, credentials, or customer-identifying content in commits.
- All changes are on a feature branch — `main` remains the stable before-state.

## Worked example: the `javax.validation` silent regression

During a Spring Boot 2.6.3 → 3.5.x upgrade, the verification gate caught a
critical silent failure in input validation:

**The setup.** The `UsersApi` controller uses `@Valid` on request bodies to
reject malformed registration payloads (empty email, blank password). In Spring
Boot 2.x, the `spring-boot-starter-validation` starter pulls `javax.validation`
(Hibernate Validator 6.x). In Boot 3.x, the same starter pulls
`jakarta.validation` (Hibernate Validator 8.x).

**The plausible mistake.** A straightforward find-and-replace of `import
javax.validation` → `import jakarta.validation` across source files compiles
cleanly. But the `build.gradle` still declares an explicit
`implementation 'javax.validation:validation-api:2.0.1.Final'` (added months ago
to fix a classpath conflict). With both jars on the classpath, Spring's
`MethodValidationPostProcessor` binds to the `jakarta` provider, while the
`@Valid` annotation on the controller resolves to `javax.validation.Valid` from
the leftover jar → validation is silently skipped.

**The catch.** `ArticlesApiTest.should_get_error_message_with_wrong_parameter`
and `UsersApiTest.should_get_error_with_invalid_registration_data` both assert
HTTP 422 for invalid payloads. After the botched upgrade, they get HTTP 200 —
the request sailed through unchecked. The test suite flags the regression
instantly:

```
UsersApiTest > should_get_error_with_invalid_registration_data FAILED
    Expected status code <422> but was <200>
```

**The fix.** Remove the explicit `javax.validation:validation-api` dependency
from `build.gradle` — the `spring-boot-starter-validation` in Boot 3.x
transitively provides `jakarta.validation-api`, and with only one provider on
the classpath, `@Valid` resolves correctly. Re-run:

```
BUILD SUCCESSFUL — 87 tests passed, 0 failed
```

The point: a human reviewer looking at a clean compile and a passing
smoke test would have shipped the broken validation. The existing test suite,
run as a gate on every change, did not.

## Advice

- **Start with the verification gate.** Run it before touching anything. If the
  baseline is red, fix that first — you cannot verify your work against a broken
  reference.
- **Upgrade in layers, not big-bang.** Verify after each layer so regressions
  are immediately attributable to the last change.
- **Treat the test suite as the contract.** If a test fails after your change,
  your change is wrong — not the test (unless the test is explicitly wrong and
  you can justify why).
- **Parallel fan-out for modernization.** Layers (`api`, `infrastructure`,
  `graphql`, `core`) are largely independent. Spawn a child session per layer
  with its own branch. The orchestrator merges and runs the full gate.
- **For security remediation, prove the fix.** Show the dependency tree before
  and after. A version bump with no evidence is not a remediation.
- **Spotless before commit.** Always `./gradlew spotlessApply` before committing
  to avoid CI formatting failures.

## Forbidden actions

- Do **not** merge feature branches into `main` — `main` is the durable
  before-state for repeatable demos.
- Do **not** disable or weaken JaCoCo thresholds to make coverage pass.
- Do **not** skip tests with `@Disabled` to make the build green.
- Do **not** modify test assertions to match broken behavior — fix the code.
- Do **not** introduce `Any`, `Object`, or unchecked casts to bypass type errors
  during the upgrade — understand and fix the actual type mismatch.
- Do **not** commit secrets, API keys, or database credentials.
- Do **not** include customer-identifying content in PRs or commit messages.
