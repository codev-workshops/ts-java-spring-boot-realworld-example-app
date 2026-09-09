# ADR 0001: Javadoc conventions for the API and service layers

- Status: Accepted
- Date: 2026-09-09

## Context

The controller (`io.spring.api`) and service layers (`io.spring.application`,
`io.spring.core.service`, `io.spring.infrastructure.service`) had no Javadoc.
Behaviour that matters to callers — which HTTP status a handler produces on
failure, how cursor pagination "peeks ahead", which helpers mutate their
arguments, how invalid JWTs are reported — was discoverable only by reading
method bodies. Adding documentation raised several decisions about *what* to
document and *how* to describe validation failures.

## Decisions

### 1. Document behaviour as implemented, not as intended

Javadoc describes what the code actually does today, even where that is
surprising (e.g. `CurrentUserApi` can throw `ArrayIndexOutOfBoundsException`
when the `Authorization` header has no scheme prefix). Documentation is not
used to paper over quirks; fixing them is a separate, behaviour-changing
change.

### 2. `@throws` names the exception type that really propagates

Controllers never throw `InvalidRequestException` themselves. Bean-validation
failures on `@Valid @RequestBody` parameters surface as Spring's
`MethodArgumentNotValidException`, and method validation on `@Validated`
services surfaces as `javax.validation.ConstraintViolationException`; both are
mapped to HTTP 422 by `CustomizeExceptionHandler`. The `@throws` tags therefore
name those two types rather than the domain-level `InvalidRequestException`.
Likewise `ResourceNotFoundException` (404) and `NoAuthorizationException` (403)
are declared only on handlers whose bodies can actually raise them.

### 3. Private methods are documented only when the logic is non-obvious

Public classes and public methods are always documented. Private helpers are
documented only where they encode behaviour a reader cannot infer from the
signature: the cursor helpers in `ArticleQueryService` (fetch `limit + 1`,
drop the extra row to derive `hasExtra`, reverse when paging backwards) and the
in-place enrichment helpers (`fillExtraInfo`, `setFavoriteCount`,
`setIsFavorite`, `setIsFollowingAuthor`) that mutate the `ArticleData`
instances passed to them.

### 4. Format is enforced by Spotless / google-java-format

Javadoc blocks follow Google Java Style and are formatted by the existing
`spotlessApply` task, so no new tooling or lint rule is introduced. The
`javadoc` Gradle task is not added to the verification gate; `spotlessCheck`
and compilation remain the gates.

## Consequences

- Callers can determine the failure modes and HTTP status of each endpoint
  from the Javadoc alone.
- Any future change to controller/service behaviour must update the adjacent
  Javadoc; stale documentation is a review concern, not a build failure.
- Documented quirks (decision 1) act as a backlog of candidate fixes.
