# Architecture Overview

## Component Map

```
custom-logging-connector/
│
├── LoggingExtension.java          Entry point. Declares extension metadata, XML prefix,
│                                  error types, config class, and pattern subtypes.
│
├── LoggingConfiguration.java      Global config block. Holds applicationName,
│                                  applicationEnvironment, and the in-memory timer cache
│                                  used by Logger Scope for elapsed time tracking.
│
├── LoggingOperations.java         All connector operations.
│   ├── customLogger()             Processes and emits a single structured log entry.
│   └── customLoggerScope()        Wraps a chain of operations; emits BEFORE/AFTER/EXCEPTION
│                                  log entries with elapsed time measurements.
│
├── singleton/ConfigsSingleton     Mule registry bean (registry-bootstrap.properties).
│                                  Bridges LoggingConfiguration instances to the scope
│                                  operation, which cannot receive @Config directly.
│
├── patterns/
│   ├── IntegrationPattern         Interface: getSelectedIntegrationPattern(), prepareData()
│   ├── HTTP.java                  HTTP-specific fields
│   ├── BATCH.java                 Batch-specific fields
│   └── MESSAGING.java             Messaging-specific fields
│
├── exception/
│   ├── LogErrorType               Enum of connector error types (INVALID_ARGUMENT)
│   └── InvalidArgumentException   ModuleException wrapper
│
└── resources/
    ├── schema/loggerProcessor.json       Defines Logger operation fields → generates POJOs
    ├── schema/loggerScopeProcessor.json  Scope-specific enum schema (ScopeTracePoint)
    └── modules/Formatter.dwl             Exported DataWeave helper functions
```

## Data Flow — Logger Operation

```
Mule Flow
   │
   ▼
customLogger()
   │
   ├── initLoggerCategory()        Sets SLF4J logger category (default or custom)
   ├── isLogEnabled()              Short-circuits if priority not enabled in log4j2
   │
   ├── PropertyUtils.describe()    Reflects LoggerProcessor POJO fields
   │   └── TypedValue handling     Streams parsed to JSON or String via TransformationService
   │
   ├── integrationPattern.prepareData()  Pattern-specific fields merged in
   │
   ├── ObjectNode assembly         Field ordering:
   │   1. LoggerProcessor fields (schema-defined order)
   │   2. locationInfo (rootContainer, component, fileName, lineInFile)
   │   3. timestamp
   │   4. String-typed content fields
   │   5. JSON-typed content fields
   │   6. threadName
   │
   └── printObjectToLog()          Serializes to JSON, dispatches to SLF4J at correct level
```

## Data Flow — Logger Scope Operation

```
Mule Flow
   │
   ▼
customLoggerScope()
   │
   ├── initLoggerCategory()
   ├── configs.getConfig()         Retrieves LoggingConfiguration from singleton
   │
   ├── getCachedTimerTimestamp()   Stores initial timestamp in ConcurrentHashMap keyed
   │                               by transactionId (first-write-wins semantics)
   │
   ├── flowListener.onComplete()   Registers cleanup runnable to remove timer from cache
   │                               (fires when the parent flow completes)
   │
   ├── BEFORE log                  Emits tracePoint=<SCOPE>_BEFORE, elapsed=0
   │
   ├── operations.process()        Executes the wrapped chain
   │   ├── onSuccess               AFTER log: scopeElapsed, mainElapsed
   │   └── onError                 EXCEPTION_SCOPE log at ERROR priority
   │
   └── callback.success/error      Returns control to Mule runtime
```

## Timer Cache

The elapsed-time mechanism uses a `ConcurrentHashMap<String, Long>` stored on the `LoggingConfiguration` bean (one per named global config). The key is the `transactionId` (defaults to `x-transaction-id` header or `correlationId`).

- `putIfAbsent` semantics ensure the first Logger Scope in a flow wins as the "start" time.
- Subsequent scopes within the same transaction compute `elapsed` against this initial timestamp.
- `flowListener.onComplete` removes the entry when the flow finishes, preventing memory leaks.

## Schema-driven POJO Generation

At Maven build time, `jsonschema2pojo-maven-plugin` reads `src/main/resources/schema/loggerProcessor.json` and generates Java classes into `target/generated-sources/`. The `CustomMuleAnnotator` (from `jsonschema2pojo-mule-annotations`) applies Mule SDK annotations (`@Parameter`, `@Optional`, `@DisplayName`, etc.) based on the `sdk` extension block in each field definition.

This means **field metadata visible in Anypoint Studio is driven entirely by the JSON schema**, not by hand-written Java annotations.
