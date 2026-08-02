package org.mulejoy.extension.logging.internal;

import org.mulejoy.extension.logging.exception.LogErrorType;
import org.mulejoy.extension.logging.patterns.BATCH;
import org.mulejoy.extension.logging.patterns.HTTP;
import org.mulejoy.extension.logging.patterns.IntegrationPattern;
import org.mulejoy.extension.logging.patterns.MESSAGING;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Export;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.license.RequiresEnterpriseLicense;

@Extension(
        name = "Custom Logging Connector",
        category = Category.SELECT
)
@RequiresEnterpriseLicense(
        allowEvaluationLicense = true
)
@ErrorTypes(LogErrorType.class)
@Xml(
        prefix = "custom-logging"
)
@Export(resources = {"modules/Formatter.dwl"})
@Configurations({LoggingConfiguration.class})
@SubTypeMapping(baseType = IntegrationPattern.class,
        subTypes = {HTTP.class, BATCH.class, MESSAGING.class})
public class LoggingExtension {

}
