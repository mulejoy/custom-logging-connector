# CLAUDE.md — AI Assistance Guide

This file provides build context, constraints, and style guidance for AI assistants working on this repository.

## Repository identity

- **Type:** MuleSoft 4 custom connector (Java SDK), packaged as `mule-extension`
- **Build tool:** Maven
- **Primary language:** Java 8
- **Secondary:** DataWeave 2.0 (`.dwl`), JSON Schema (`.json`)
- **Minimum Mule version:** 4.3.0

## Build commands

```bash
# Compile and package
mvn clean package

# Package skipping tests
mvn clean package -DskipTests

# Deploy to Anypoint Exchange (requires credentials)
./scripts/deploy.sh

# Run tests
mvn test
```

## Project layout

```
src/main/java/org/mulejoy/extension/logging/
  internal/           # Core connector wiring (Extension, Configuration, Operations)
  internal/singleton/ # ConfigsSingleton — bridges config to scope operations
  patterns/           # Integration pattern subtypes (HTTP, BATCH, MESSAGING)
  exception/          # Custom error types

src/main/resources/
  schema/             # JSON Schema definitions — primary customization point
  modules/            # Exported DataWeave modules (Formatter.dwl)
  META-INF/           # Mule registry bootstrap

docs/                 # Architecture and technical documentation
templates/            # Client-ready configuration templates
scripts/              # Deployment helpers
```

## Namespace

All Java source lives under `org.mulejoy.extension.logging`. Never introduce `org.liem` or any personal namespace.

## Key architectural constraints

- **Schema-first for log fields:** Add/remove logged fields by editing `src/main/resources/schema/loggerProcessor.json`. The `jsonschema2pojo-maven-plugin` generates the Java POJOs at build time into `target/generated-sources`. Do not hand-write POJOs for log fields.
- **Singleton pattern for scope config:** The Mule SDK does not support passing `@Config` into Scope operations. `ConfigsSingleton` is the registered bridge — it is a Mule registry bean (see `registry-bootstrap.properties`). Do not remove it or attempt to pass config directly into `customLoggerScope`.
- **Non-blocking I/O:** Both `customLogger` and `customLoggerScope` use `CompletionCallback` for NIO. Never convert these to synchronous return types.
- **No `System.out.println`:** All diagnostic output must go through SLF4J (`logger.debug(...)`, etc.). Debug-level statements are acceptable for diagnostic tracing but must be guarded by `logger.isDebugEnabled()` if they involve string concatenation in hot paths.

## Style guide

- No inline comments explaining what code does — only WHY when non-obvious
- No multi-line Javadoc blocks on private methods
- No dead commented-out code
- Getters/setters on pattern classes may be one-liners on a single line
- `HashMap<>()` over `new HashMap<String, String>()` (use diamond operator)
- Keep `LoggingOperations.java` lean — business logic belongs in helpers or pattern classes

## Extending integration patterns

To add a new integration pattern (e.g., `SCHEDULER`):

1. Create `src/main/java/org/mulejoy/extension/logging/patterns/SCHEDULER.java` implementing `IntegrationPattern`
2. Add it to `@SubTypeMapping` in `LoggingExtension.java`
3. No schema changes needed — pattern fields are handled separately from `loggerProcessor.json`

## Things to avoid

- Do not hardcode organization IDs, personal namespaces, or client-specific values
- Do not add dependencies without confirming Mule SDK compatibility
- Do not change the XML prefix `custom-logging` — it is part of the published connector contract
- Do not modify `Formatter.dwl` function signatures — consuming applications depend on them
