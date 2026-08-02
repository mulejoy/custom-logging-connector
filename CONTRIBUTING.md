# Contributing Guide

This document is for consultants building client-specific extensions on top of this connector. The goal is to enable safe customization without breaking core IP or making the connector non-reusable across engagements.

---

## Ground rules

1. **Core IP is protected.** Do not commit client-specific business logic, client names, or client org IDs into the `main` branch of this repository.
2. **Schema is the primary extension point.** Add log fields by editing JSON schema, not by modifying Java classes.
3. **All changes must build cleanly** with `mvn clean package` before being considered complete.
4. **Branch strategy:** Work in a client-named branch (e.g., `client/acme-corp`). Merge generic improvements back to `main` via PR. Keep client-specific changes on the client branch.

---

## Adding or modifying log fields

Edit `src/main/resources/schema/loggerProcessor.json`. The schema follows JSON Schema Draft 4 with Mule SDK extensions under the `sdk` key.

**Example — adding a `businessDomain` string field:**

```json
"businessDomain": {
  "type": "string",
  "sdk": {
    "displayName": "Business Domain",
    "required": false,
    "summary": "Business domain owning this flow (e.g., Order Management)"
  }
}
```

Run `mvn clean package` — the POJO is generated automatically. No Java changes needed.

**Constraints:**
- Do not remove `priority` or `category` — they are marked `DON'T REMOVE` in the schema and control runtime behavior
- Do not change `javaType` values for enum fields without also creating the corresponding Java enum class

---

## Adding a new integration pattern

1. Create `src/main/java/org/mulejoy/extension/logging/patterns/YOURPATTERN.java` implementing `IntegrationPattern`:

```java
package org.mulejoy.extension.logging.patterns;

import java.util.HashMap;

public class YOURPATTERN implements IntegrationPattern {

    @Override
    public String getSelectedIntegrationPattern() {
        return "YOURPATTERN";
    }

    @Override
    public HashMap<String, Object> prepareData() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("pattern", this.getSelectedIntegrationPattern());
        // add pattern-specific fields here
        return data;
    }
}
```

2. Register it in `LoggingExtension.java`:

```java
@SubTypeMapping(baseType = IntegrationPattern.class,
        subTypes = {HTTP.class, BATCH.class, MESSAGING.class, YOURPATTERN.class})
```

3. Build and test: `mvn clean package`

---

## Client-specific delivery workflow

```
main (core IP) ──────────────────────────────────────────────►
                   │
                   └── client/acme-corp ──► deploy to Acme Exchange
                   │
                   └── client/globex ────► deploy to Globex Exchange
```

**Steps:**

1. Branch from `main`: `git checkout -b client/CLIENT_NAME`
2. Update `pom.xml`: set `<groupId>` to client's Anypoint Org ID, bump version if needed
3. Make any client-specific schema customizations
4. Deploy: `./scripts/deploy.sh`
5. If a change is generic and reusable, open a PR to merge it into `main` with the client-specific values reverted

---

## What NOT to commit to `main`

- Client org IDs (keep `YOUR_ANYPOINT_ORG_ID` as the placeholder)
- Client-specific field names that only apply to one engagement
- Credentials or environment-specific properties
- Commented-out code or debugging artifacts

---

## Testing

Unit testing for Mule connectors requires the MuleSoft Test Compatibility Kit (TCK). For now, verify behavior by deploying to a development Anypoint environment and exercising the connector from a test Mule application. A guide for writing MUnit-based integration tests is a planned addition.
