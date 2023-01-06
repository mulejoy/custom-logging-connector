package org.liem.extension.logging.internal;

import org.liem.extension.logging.exception.LogErrorType;
import org.liem.extension.logging.patterns.BATCH;
import org.liem.extension.logging.patterns.HTTP;
import org.liem.extension.logging.patterns.IntegrationPattern;
import org.liem.extension.logging.patterns.MESSAGING;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Export;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.license.RequiresEnterpriseLicense;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
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