# Custom Logging Connector

**Mulejoy Consulting IP** | Mule 4 SDK Extension | v1.0.0

---

## Executive Summary

The Custom Logging Connector is a production-grade MuleSoft 4 custom connector that replaces ad-hoc usage of the default Logger and community JSON Logger with a **structured, integration-pattern-aware logging framework**. It enforces a standardized JSON log schema across all Mule 4 applications within an organization, eliminating inconsistent logging decisions, reducing operational blind spots, and enabling reliable log aggregation in platforms such as Splunk, Datadog, Elasticsearch, or Dynatrace.

**Key business outcomes:**
- Mandate correlation fields (transaction ID, flow name, environment, timestamp) on every log entry
- Eliminate per-developer logging variability — one connector, one enforced schema
- Enable end-to-end transaction tracing without ad-hoc log schema negotiation
- Capture performance metrics (total elapsed time, scope elapsed time) out of the box
- Reduce Anypoint Studio onboarding friction via guided, strongly-typed connector parameters

---

## Architecture Overview

See [`docs/architecture.md`](docs/architecture.md) for a detailed component breakdown. Summary:

```
┌──────────────────────────────────────────────────────────────────┐
│                      Mule 4 Application                          │
│                                                                  │
│  ┌────────────────────┐     ┌────────────────────────────────┐  │
│  │  Logger Operation  │     │       Logger Scope Operation   │  │
│  │  (customLogger)    │     │       (customLoggerScope)      │  │
│  │                    │     │                                │  │
│  │ • Integration      │     │ • BEFORE / AFTER trace points  │  │
│  │   Pattern fields   │     │ • Scope elapsed time           │  │
│  │ • Payload content  │     │ • Exception capture            │  │
│  │ • Trace point      │     │                                │  │
│  └────────┬───────────┘     └──────────────┬─────────────────┘  │
│           │                                │                     │
│           └──────────────┬─────────────────┘                     │
│                          ▼                                        │
│              ┌───────────────────────┐                           │
│              │  JSON Log Output      │                           │
│              │  via SLF4J / Log4j2   │                           │
│              └───────────────────────┘                           │
└──────────────────────────────────────────────────────────────────┘
```

### Integration Patterns

The connector is aware of three integration patterns, each contributing pattern-specific fields to the log entry:

| Pattern | Key Fields |
|---------|-----------|
| `HTTP` | method, resource path, client ID, scheme, query params, URI params, remote address, HTTP status |
| `BATCH` | batch record ID |
| `MESSAGING` | message headers / properties |

### Schema-driven POJO generation

Log entry fields for the Logger operation are defined in `src/main/resources/schema/loggerProcessor.json`. At build time, `jsonschema2pojo-maven-plugin` generates strongly-typed Java POJOs annotated with Mule SDK metadata. **Modifying the JSON schema is the primary extension point** — no Java changes required for field additions.

### Formatter.dwl

A DataWeave module (`modules/Formatter.dwl`) is exported by the connector and available to consuming Mule applications. It provides helper functions for formatting payloads before logging:

| Function | Behavior |
|----------|----------|
| `formatAny` | Serializes any payload type to a string suitable for logging |
| `formatNonJSON` | Like `formatAny` but passes JSON through as-is (no double-serialization) |
| `formatAnyWithMetadata` | Wraps payload with `contentLength`, `dataType`, and `class` metadata |
| `formatNonJSONWithMetadata` | Same as above but JSON-passthrough variant |

---

## Quickstart Guide

### Prerequisites

- Java 8+
- Maven 3.6+
- Access to an Anypoint Platform organization (MuleSoft Mule 4 EE license or evaluation)
- MuleSoft Maven settings configured (see [MuleSoft Maven configuration](https://docs.mulesoft.com/mule-runtime/latest/maven-reference))

### 1. Configure your organization ID

Edit `pom.xml` and replace `YOUR_ANYPOINT_ORG_ID` with your Anypoint Platform Organization ID:

```xml
<groupId>YOUR_ANYPOINT_ORG_ID</groupId>
```

Your Org ID is visible in Anypoint Platform under **Access Management → Organization → Organization ID**.

### 2. Build locally

```bash
mvn clean package
```

The built connector JAR will be in `target/`.

### 3. Deploy to Anypoint Exchange

```bash
./scripts/deploy.sh
```

Or manually:

```bash
mvn deploy \
  -DskipTests \
  -Danypoint.username=$ANYPOINT_USERNAME \
  -Danypoint.password=$ANYPOINT_PASSWORD
```

> **Important:** You cannot publish the same version twice to Exchange. Either delete the existing asset within 7 days or bump the version in `pom.xml` before redeploying.

### 4. Add the connector to a Mule application

In the consuming application's `pom.xml`, add:

```xml
<dependency>
    <groupId>YOUR_ANYPOINT_ORG_ID</groupId>
    <artifactId>custom-logging-connector</artifactId>
    <version>1.0.0</version>
    <classifier>mule-plugin</classifier>
</dependency>
```

See [`templates/pom-client-template.xml`](templates/pom-client-template.xml) for a complete snippet.

### 5. Configure the connector

```xml
<custom-logging:config
    name="Custom_Logging_Connector_Config"
    applicationName="${app.name}"
    applicationEnvironment="${mule.env}"/>
```

### 6. Use the Logger operation

```xml
<custom-logging:custom-logger
    config-ref="Custom_Logging_Connector_Config"
    message="Processing started"
    tracePoint="START"
    priority="INFO">
    <custom-logging:integration-pattern>
        <custom-logging:http/>
    </custom-logging:integration-pattern>
</custom-logging:custom-logger>
```

### 7. Use the Logger Scope (for elapsed time tracking)

```xml
<custom-logging:custom-logger-scope
    configurationRef="Custom_Logging_Connector_Config"
    scopeTracePoint="OUTBOUND_REQUEST_SCOPE"
    priority="INFO">
    <!-- operations to measure -->
    <http:request .../>
</custom-logging:custom-logger-scope>
```

### Runtime JVM options

| Property | Default | Description |
|----------|---------|-------------|
| `json.logger.timezone` | `UTC` | Timestamp timezone. Use any [IANA tz name](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones). |
| `json.logger.dateformat` | ISO 8601 | Custom date format string (Joda-Time pattern). |

---

## Consulting Delivery Playbook

### Engagement checklist

- [ ] Obtain client's Anypoint Organization ID
- [ ] Set `groupId` in `pom.xml` to client's Org ID
- [ ] Align on log aggregation platform (Splunk, Datadog, ELK, etc.) and confirm JSON log format compatibility
- [ ] Review `loggerProcessor.json` with client — add, remove, or rename fields to match their standard
- [ ] Configure client's `log4j2.xml` using [`templates/log4j2-client-template.xml`](templates/log4j2-client-template.xml) as a baseline
- [ ] Deploy to client's Exchange (dev, staging, prod orgs as applicable)
- [ ] Provide client developers with the dependency snippet from [`templates/pom-client-template.xml`](templates/pom-client-template.xml)
- [ ] Validate end-to-end: trigger a flow, confirm structured JSON appears in the log aggregator

### Customization guide

**To add a new log field:** Edit `src/main/resources/schema/loggerProcessor.json` — add a property entry following the existing pattern. Rebuild and redeploy. No Java changes required.

**To add a new integration pattern:** Create a new class in `src/main/java/org/mulejoy/extension/logging/patterns/` implementing `IntegrationPattern`. Register it in the `@SubTypeMapping` annotation on `LoggingExtension`.

**To change the log format from pretty-print to single-line:** In `LoggingOperations.java`, change the `isPrettyPrint` argument in `printObjectToLog(...)` calls from `true` to `false`. Single-line JSON is preferred for most log aggregators.

**To change the timestamp format globally:** Pass `-Djson.logger.dateformat=yyyy-MM-dd'T'HH:mm:ss.SSSZ` to the Mule runtime JVM arguments, or set `json.logger.timezone` for timezone-only changes.

### Versioning policy

Bump the `<version>` in `pom.xml` for every client deployment iteration. Use semantic versioning: `MAJOR.MINOR.PATCH`.

---

## IP Disclaimer

This connector is **proprietary consulting IP** owned by Mulejoy. It is licensed for use within the scope of a specific client engagement. Redistribution, resale, or use outside the agreed engagement scope is not permitted without written authorization from Mulejoy.

This connector is not affiliated with, endorsed by, or supported by MuleSoft or Salesforce. It is a community/custom extension built on the public MuleSoft Java SDK.
