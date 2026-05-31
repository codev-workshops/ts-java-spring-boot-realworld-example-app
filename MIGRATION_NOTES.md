# Java 8 to 11 Migration Notes

## Summary

This document captures the changes made to migrate the project from Java 8 to Java 11 (LTS).

## Build Configuration

| Item | Before | After |
|------|--------|-------|
| Java target | `sourceCompatibility = '11'` / `targetCompatibility = '11'` | Java toolchain `languageVersion = 11` |
| Gradle wrapper | 7.4 | 7.6.4 |
| JaCoCo | 0.8.7 | 0.8.11 |
| Compiler args | none | `-Xlint:all`, `-Xlint:-processing`, `-Xlint:-serial` |
| Source encoding | implicit | UTF-8 (explicit) |

## CI/CD

| Item | Before | After |
|------|--------|-------|
| JDK distribution | Zulu | Temurin (Adoptium) |
| JDK version | 11 | 11 |
| Caching | Manual `actions/cache` | Built-in `setup-java` Gradle caching |

## Removed JDK Module Analysis

The codebase was analyzed for usage of modules removed in Java 11:

- **JAXB (`javax.xml.bind`)**: Not used
- **JAX-WS (`javax.xml.ws`)**: Not used
- **CORBA**: Not used
- **JavaFX**: Not used
- **Nashorn**: Not used
- **`sun.misc` / internal APIs**: Not used

No dependency replacements were needed.

## Encapsulation (JPMS)

The project runs on the **classpath** (no `module-info.java`). No illegal reflective access warnings were observed during testing. No `--add-opens` flags are required.

## Security / TLS

- Java 11 enables TLS 1.3 by default.
- No JKS keystore migration was needed (project uses SQLite, no external TLS endpoints in core app).
- Default keystore type is now PKCS12 (no JKS stores in this project).

## GC / Runtime

- Default GC is G1 (since Java 9). No legacy GC flags were in use.
- No obsolete JVM flags needed removal.
- Unified GC logging is available via `-Xlog:gc*` if needed for production tuning.

## Known Issues

None. All tests pass on JDK 11.

## Follow-up Opportunities

- Consider upgrading to Spring Boot 2.7.x or 3.x (requires Java 17 for 3.x)
- Evaluate `var` (local variable type inference, Java 10+) for readability
- Consider `java.net.http.HttpClient` (Java 11) if HTTP client code is added
- Evaluate Gradle 8.x upgrade once DGS codegen plugin compatibility is confirmed
