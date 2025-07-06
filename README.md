# Custom Logging Connector

This is a replacement for the default Mule Logger or a JSON logger. It is more descriptive, flexible and integration pattern driven logger that can be easily configured or modified according to the organization needs.

## Why me?
* Logs are essential for a better operations. Developers tend to miss the right choices.
* Every developer has their own decision-making on what needs to be logged, so we standardized it.
* Mule doesn't offer out-of-the-box (OOB) logging standards.
* Mandate the essential attributes to be logged to identify a transaction end-to-end.
* General performance tracking.

## What did we do?
A Java SDK based Mule 4 connector has been built to overcome the industry challenges with respect to logging in Mulesoft.

Also, this maximizes customization to each customer's requirements while avoiding steep Java SDK learning curves, so you can easily modify the output JSON data structure as well as connector configuration by editing 2 simple JSON schemas provided under:
>/src/main/resources/schema/

In a nutshell, by defining the output JSON schema as well as providing some additional SDK specific details (e.g. default values, default expressions, etc.), we can dynamically generate a module that aligns to those schemas.

**Note**: jsonschema2pojo-mule-annotations is a CustomMuleAnnotator written by Mulejoy.
The below dependency is already included as part of the connector and shown here only for reference.

```
<dependency>
    <groupId>io.github.mulesoft-consulting</groupId>
    <artifactId>jsonschema2pojo-mule-annotations</artifactId>
    <version>1.2.0</version>
</dependency>
```

## How do you use me?

Configure the Connector dependency as below <br>
_ORG_ID_ must be replaced with your respective Organization ID

```
<groupId>ORG_ID</groupId>
<artifactId>custom-logging-connector</artifactId>
<version>1.0.0</version>
<classifier>mule-plugin</classifier>
```

Also, ensure that your distributionManagement in pom.xml is configured with the exchange repository pointing to your organization<br>
```
<distributionManagement>
    <!-- Target Anypoint Organization Repository -->
    <repository>
        <id>Exchange2</id>
        <name>Exchange2 Repository</name>
        <url>https://maven.anypoint.mulesoft.com/api/v1/organizations/${project.groupId}/maven</url>
        <layout>default</layout>
    </repository>
</distributionManagement>
```

Once the above-mentioned configurations are enabled in your pom.xml, execution of maven deploy goal _**'mvn deploy'**_ should be able to publish the Connector asset to your organization.

**IMPORTANT:** You have to manually delete the previous asset from your exchange within 7 days of deployment since you can't deploy the same version to Exchange;
or increase the version in the pom.xml to publish a newer version of the asset.

## 1.0.0 version - Release notes

**Features:**
* Different attributes against a specific integration pattern
* Logs request and response payload content
* Capture additional details that enriches your logging information such as functional KPIs, like object type processed, success record count, failed record count, and skipped record count
* Scoped loggers to capture "scope bound elapsed time", so these are your go-to options for performance tracking of specific components, like database or REST consumer calls
* Loggers clearly capture a Mule application's integration pattern
* Developers can decide whether the payload needs to be printed or not, which ensures that a large payload being printed in logs can be skipped by an informed decision from the developer

## Author

## Support disclaimer
It won't be officially supported by MuleSoft as it is considered a custom connector.
