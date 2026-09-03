---
name: java-engineering-excellence
description: >
  Repo-specific mechanics for the Java Engineering Excellence playbook on
  ts-java-spring-boot-realworld: build commands, verification gates, package
  layout, dependency coordinates, and branching conventions.
---

# Java Engineering Excellence — Repo Mechanics

## Build & Verification Commands

```bash
# Compile (no tests) — confirm build is healthy
./gradlew clean build -x test

# Full verification gate (the bar for every PR)
./gradlew clean test spotlessCheck

# Auto-format before committing
./gradlew spotlessApply

# Coverage report + enforcement (80% line coverage minimum)
./gradlew jacocoTestReport jacocoTestCoverageVerification

# Single test class (fast feedback)
./gradlew test --tests "io.spring.api.ArticlesApiTest"

# Dependency insight (for CVE triage)
./gradlew dependencyInsight --dependency <group:artifact>

# Selenium E2E tests (separate task, TestNG — excluded from main gate)
./gradlew seleniumTest
```

## Project Layout

```
ts-java-spring-boot-realworld/
├── build.gradle              # Gradle build (Spring Boot 2.6.3, Java 11)
├── gradlew / gradlew.bat     # Gradle wrapper
├── src/main/java/io/spring/
│   ├── api/                  # REST controllers (@RestController)
│   │   ├── security/         # WebSecurityConfig, JwtTokenFilter
│   │   └── exception/        # @ControllerAdvice, custom exceptions
│   ├── application/          # Query services, DTOs, command services
│   │   ├── article/          # ArticleCommandService
│   │   ├── data/             # ArticleData, ProfileData, CommentData
│   │   └── user/             # UserQueryService
│   ├── core/                 # Domain entities and service interfaces
│   │   ├── article/          # Article, Tag, ArticleRepository
│   │   ├── comment/          # Comment, CommentRepository
│   │   ├── favorite/         # ArticleFavorite, ArticleFavoriteRepository
│   │   ├── service/          # JwtService interface
│   │   └── user/             # User, UserRepository, FollowRelation
│   ├── graphql/              # DGS GraphQL datafetchers + mutations
│   │   └── exception/        # GraphQL error handling
│   └── infrastructure/       # Implementation layer
│       ├── mybatis/mapper/   # MyBatis mapper interfaces
│       ├── mybatis/readservice/ # Read-model query services
│       ├── repository/       # MyBatis-backed repository impls
│       └── service/          # DefaultJwtService (jjwt)
├── src/main/resources/
│   ├── db/migration/         # Flyway SQL migrations (V1__, V2__, ...)
│   ├── mapper/               # MyBatis XML mapper files
│   ├── schema/               # GraphQL schema (.graphqls)
│   └── application.properties
├── src/test/java/io/spring/
│   ├── api/                  # REST API tests (JUnit 5 + REST-Assured MockMvc)
│   ├── infrastructure/       # Repository + service integration tests
│   └── selenium/             # Selenium E2E tests (TestNG, excluded from gate)
└── frontend/                 # Next.js frontend (separate, optional)
```

## Key Dependencies (current baseline — Spring Boot 2.6.3 / Java 11)

| Artifact | Version | Upgrade Target (Boot 3.5.x) |
|----------|---------|----------------------------|
| `org.springframework.boot` (plugin) | 2.6.3 | 3.5.x |
| `io.spring.dependency-management` | 1.0.11 | 1.1.x |
| Java `sourceCompatibility` | 11 | 21 |
| `com.netflix.dgs.codegen` | 5.0.6 | 7.x+ |
| `com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter` | 4.9.21 | 8.x+ (Boot 3 compatible) |
| `org.mybatis.spring.boot:mybatis-spring-boot-starter` | 2.2.2 | 3.0.x |
| `io.jsonwebtoken:jjwt-api` | 0.11.2 | 0.12.x |
| `joda-time:joda-time` | 2.10.13 | remove → `java.time` |
| `org.xerial:sqlite-jdbc` | 3.36.0.3 | 3.45.x+ |
| `io.rest-assured:spring-mock-mvc` (test) | 4.5.1 | 5.4.x |
| `com.diffplug.spotless` | 6.2.1 | 6.25.x |
| `org.flywaydb:flyway-core` | (managed) | 10.x (Boot 3.5 managed) |
| Selenium (test) | 4.15.0 | 4.20.x |

## General Approach: Java 11 → 21 / Spring Boot 2.x → 3.x Upgrades

### Strategy: Layer-by-Layer with Continuous Verification

Major version upgrades touch many files. The safe approach is incremental — one
layer at a time, with the test suite gating each step. Never upgrade everything
at once; a failing test after a 200-file change is nearly impossible to
diagnose.

**Recommended layer order:**

1. **Build config first** — Gradle plugins, Java version, Spring Boot parent.
   This makes compilation fail loudly for anything incompatible, exposing the
   full scope of work. Run `./gradlew clean build -x test` to see what breaks.

2. **Namespace migration** — `javax.*` → `jakarta.*`. This is mechanical but
   dangerous: if you leave a stale explicit `javax` dependency on the classpath,
   both providers exist and Spring silently binds to the wrong one. The test
   suite will catch this (validation returns 200 instead of 422). Always remove
   explicit `javax.validation:validation-api` or `javax.servlet:javax.servlet-api`
   from `build.gradle` after migrating imports.

3. **Security config** — `WebSecurityConfigurerAdapter` is removed in Boot 3.
   Replace with a `@Bean SecurityFilterChain` using the lambda DSL. Update
   `antMatchers` → `requestMatchers`, `authorizeRequests` → `authorizeHttpRequests`.

4. **Infrastructure** — MyBatis, Flyway, database drivers, JWT libraries. These
   typically have straightforward upgrade paths but may change package names or
   method signatures.

5. **GraphQL / DGS** — DGS 8.x is Boot 3 compatible. The codegen plugin version
   must also be bumped. Schema files are unchanged; only the generated code and
   runtime wiring differ.

6. **Date/time** — `joda-time` → `java.time`. Replace `DateTime` with `Instant`
   or `OffsetDateTime`. Remove the joda dependency entirely.

**After each layer**, run `./gradlew clean test spotlessCheck`. Fix any failures
before proceeding to the next layer. The test suite is the contract — if it
breaks, the upgrade introduced a regression.

### Known Pitfall: Dual Validation Providers

The most dangerous silent failure in a Boot 2→3 upgrade: after changing imports
to `jakarta.validation`, an explicit `javax.validation:validation-api` dependency
left in `build.gradle` puts two providers on the classpath. Spring's validator
binds to `jakarta`, but `@Valid` annotations on controllers resolve from the
stale `javax` jar — validation is silently skipped. Tests that assert HTTP 422
on invalid input catch this immediately.

### Namespace Migration Checklist

1. `javax.validation.*` → `jakarta.validation.*`
2. `javax.servlet.*` → `jakarta.servlet.*`
3. `javax.persistence.*` → `jakarta.persistence.*` (if JPA added)
4. `javax.annotation.*` → `jakarta.annotation.*`
5. Remove explicit `javax.validation:validation-api` from `build.gradle`
6. `WebSecurityConfigurerAdapter` → `@Bean SecurityFilterChain`
7. `.antMatchers(...)` → `.requestMatchers(...)`
8. `authorizeRequests()` → `authorizeHttpRequests()`
9. Update `@MockBean` → Spring Boot 3.4+ deprecation path (if applicable)

## Branching Convention

```bash
# Feature branch pattern (never merge to main for demos)
git checkout -b devin/$(date +%s)-<task-type>-<short-desc>

# Examples:
git checkout -b devin/1718451200-feature-dev-bookmarks
git checkout -b devin/1718451200-cve-remediation-jackson
git checkout -b devin/1718451200-app-modernization-boot3
git checkout -b devin/1718451200-test-generation-coverage
```

## Verification Gate — What "Green" Means

```
BUILD SUCCESSFUL in 42s
87 tests completed, 0 failed
Spotless: 120 files checked — no violations
JaCoCo: line coverage 82.3% (minimum 80.0%) — PASS
```

If any of these fail, the PR is not ready.

## Parallel Fan-Out — Layer Namespaces

For the app-modernization task, work can be parallelized by layer. Each child
session takes one layer, creates its own branch, and opens its own PR:

| Child | Layer | Packages | Branch suffix |
|-------|-------|----------|---------------|
| 1 | API + Security | `io.spring.api.*` | `-upgrade-api` |
| 2 | Infrastructure | `io.spring.infrastructure.*` | `-upgrade-infra` |
| 3 | GraphQL | `io.spring.graphql.*` | `-upgrade-graphql` |
| 4 | Core + Application | `io.spring.core.*`, `io.spring.application.*` | `-upgrade-core` |

The orchestrator merges all four branches, runs the full gate, and opens the
final consolidated PR.

## Security Scanning (SAST)

The repo includes the OWASP Dependency-Check Gradle plugin and a GitHub Action
workflow (`.github/workflows/dependency-check.yml`) that:

1. Runs weekly (Monday 06:00 UTC) and on manual trigger
2. Executes `./gradlew dependencyCheckAnalyze` against the NVD database
3. Creates GitHub issues labeled `security` for findings with CVSS ≥ 7.0
4. A Devin Automation watches for `security`-labeled issues and starts a
   remediation session automatically

```bash
# Quick dependency insight (no plugin needed)
./gradlew dependencies --configuration runtimeClasspath | grep -i "jackson\|log4j\|spring-"

# Detailed insight for a specific artifact
./gradlew dependencyInsight --dependency com.fasterxml.jackson.core:jackson-databind

# Known critical CVEs in baseline:
# - Spring Framework 5.3.15 (CVE-2022-22965 — Spring4Shell)
# - jackson-databind 2.13.x (multiple RCE vectors)
# - joda-time (EOL, no security patches)
# - sqlite-jdbc 3.36.x (buffer overflow CVEs)
```

### Automation Flow

```
SAST scan (weekly) → Finds CVE → Creates GitHub issue (labeled: security)
    → Devin Automation triggers → Session runs !java_engineering_excellence
    → task: issue-triage → Remediation PR opened → Team reviews
```

## Test Patterns (follow existing conventions)

**Controller tests** — `@WebMvcTest` + `@Import(WebSecurityConfig.class)` +
REST-Assured `MockMvc`:
```java
@WebMvcTest({ArticlesApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticlesApiTest extends TestWithCurrentUser { ... }
```

**Repository tests** — extend `DbTestBase` (H2 in-memory):
```java
public class MyBatisArticleRepositoryTest extends DbTestBase { ... }
```

**Service tests** — plain JUnit 5 + Mockito:
```java
@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceTest { ... }
```

## Revert / Reset (for repeatable demos)

The before-state is always `main`. To reset after a demo run:

```bash
git checkout main
git branch -D devin/*   # remove all demo branches locally
```

Remote branches (PRs) can be closed without merging. The database is recreated
on each `bootRun` (Flyway + clean task), so no data cleanup is needed.
