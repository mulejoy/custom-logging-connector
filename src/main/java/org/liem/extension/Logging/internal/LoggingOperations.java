package org.liem.extension.logging.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.joda.time.DateTime;
import org.liem.extension.logging.api.pojos.LoggerProcessor;
import org.liem.extension.logging.api.pojos.Priority;
import org.liem.extension.logging.api.pojos.ScopeTracePoint;
import org.liem.extension.logging.internal.singleton.ConfigsSingleton;
import org.liem.extension.logging.patterns.IntegrationPattern;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.meta.model.operation.ExecutionType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.execution.Execution;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.FlowListener;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.api.metadata.DataType.TEXT_STRING;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class LoggingOperations {
	protected transient org.slf4j.Logger jsonLogger;
	private static final org.slf4j.Logger logger =  LoggerFactory.getLogger(LoggingOperations.class);

	// Global definition of logger configs so that it's available for scope processor (SDK scope doesn't support passing configurations)
	@Inject
	ConfigsSingleton configs;

	// Transformation Service
	@Inject
	private TransformationService transformationService;
	// Void Result for NIO
	private final Result<Void, Void> VOID_RESULT = Result.<Void, Void>builder().build();

	ObjectMapper om = new ObjectMapper();

	@DisplayName("Logger")
	@MediaType(value = "*/*", strict = false)
	@Alias("customLogger")
	@Execution(ExecutionType.BLOCKING)
	public void customLogger(
			@Expression(value = NOT_SUPPORTED) IntegrationPattern integrationPattern,
			@ParameterGroup(name = "Logger") @Expression(value = NOT_SUPPORTED) LoggerProcessor loggerProcessor,
			@Config LoggingConfiguration config,
			ComponentLocation location,
			FlowListener flowListener,
			CompletionCallback<Void, Void> callback
	) throws IOException {
		Long initialTimestamp, loggerTimestamp;
		initialTimestamp = loggerTimestamp = System.currentTimeMillis();

		initLoggerCategory(loggerProcessor.getCategory());

		/**
		 * Avoid Logger logic execution based on log priority
		 */
		if (isLogEnabled(loggerProcessor.getPriority().toString())) {
			Map<String, String> typedValuesAsString = new HashMap<>();
			Map<String, JsonNode> typedValuesAsJsonNode = new HashMap<>();
			try {
				PropertyUtils.describe(loggerProcessor).forEach((k, v) -> {
						if (v != null) {
							try {
								if (v instanceof ParameterResolver) {
									v = ((ParameterResolver) v).resolve();
								}
								if (v.getClass().getCanonicalName().equals("org.mule.runtime.api.metadata.TypedValue")) {
									logger.debug("org.mule.runtime.api.metadata.TypedValue type was found for field: " + k);
									TypedValue<InputStream> typedVal = (TypedValue<InputStream>) v;
									logger.debug("Parsing TypedValue for field " + k);

									logger.debug("TypedValue MediaType: " + typedVal.getDataType().getMediaType());
									logger.debug("TypedValue Type: " + typedVal.getDataType().getType().getCanonicalName());
									logger.debug("TypedValue Class: " + typedVal.getValue().getClass().getCanonicalName());

									// Remove unparsed field
									BeanUtils.setProperty(loggerProcessor, k, null);

									// Evaluate if typedValue is null
									if (typedVal.getValue() != null) {
										if (typedVal.getDataType().getMediaType().getPrimaryType().equals("application") && typedVal.getDataType().getMediaType().getSubType().equals("json")) {
											typedValuesAsJsonNode.put(k, om.readTree((InputStream)typedVal.getValue()));
										} else {
											typedValuesAsString.put(k, (String) transformationService.transform(typedVal.getValue(), typedVal.getDataType(), TEXT_STRING));
										}
									}
								}
							} catch (Exception e) {
								logger.error("Failed parsing field: " + k, e);
								typedValuesAsString.put(k, "Error parsing expression. See logs for details.");
							}
					}
				});

			} catch (Exception e) {
				logger.error("Unknown error while processing the logger object", e);
			}

			// Integration Pattern data
			integrationPattern.prepareData().forEach((k, v) -> {
				if (v != null) {
					logger.debug("Class Name: " + v.getClass().getCanonicalName());
					if (v.getClass().getCanonicalName().contains("Stream")) {
						try {
							typedValuesAsJsonNode.put(k, om.readTree((InputStream)v));
						} catch (IOException e) {
							logger.error("Failed parsing field: " + k, e);
						}
					} else {
						typedValuesAsString.put(k, (String) v);
					}
				}
			});

			// Aggregate Logger data into mergedLogger
			ObjectNode mergedLogger = om.createObjectNode();
			mergedLogger.setAll((ObjectNode) om.valueToTree(loggerProcessor));

			/**
			 * Custom field ordering for Logger Operation
			 * ==========================================
			 * This will take place after LoggerProcessor ordering which is defined by the field sequence in loggerProcessor.json
			 **/

			// Location Info: Logger location within Mule application

			Map<String, String> locationInfo = locationInfoToMap(location);
			mergedLogger.putPOJO("locationInfo", locationInfo);

			// Timestamp: Add formatted timestamp entry to the logger
			mergedLogger.put("timestamp", getFormattedTimestamp(loggerTimestamp));
			// Content fields: String based fields
			if (!typedValuesAsString.isEmpty()) {
				JsonNode typedValuesNode = om.valueToTree(typedValuesAsString);
				mergedLogger.setAll((ObjectNode) typedValuesNode);
			}
			// Content fields: JSONNode based fields
			if (!typedValuesAsJsonNode.isEmpty()) {
				mergedLogger.setAll(typedValuesAsJsonNode);
			}

			// Thread Name
			mergedLogger.put("threadName", Thread.currentThread().getName());
			/** End field ordering **/

			/** Print Logger **/
			printObjectToLog(mergedLogger, loggerProcessor.getPriority().toString(), true);

		} else {
			logger.debug("Avoiding logger operation logic execution due to log priority not being enabled");
		}
		callback.success(VOID_RESULT);
	}

	/**
	 * Logger scope to measure transactions
	 */
	@DisplayName("Logger Scope")
	@Execution(ExecutionType.BLOCKING)
	public void customLoggerScope(
			@DisplayName("Module configuration") @Example("Custom_Logging_Connector_Config") @Summary("Indicate which Global config should be associated with this Scope.") String configurationRef,
			@Optional(defaultValue="INFO") Priority priority,
			@Optional(defaultValue="OUTBOUND_REQUEST_SCOPE") ScopeTracePoint scopeTracePoint,
			@Optional @Summary("If not set, by default will log to the org.liem.extension.logging.CustomLogger category") String category,
			@Optional(defaultValue="#[attributes.headers.'x-transaction-id' default correlationId]")
			@Placement(tab = "Advanced") @DisplayName("Transaction ID") String transactionId,
			@Placement(tab = "Advanced") @Optional(defaultValue = "#[flow.name]") String flowName,
			ComponentLocation location,
			CorrelationInfo correlationInfo,
			FlowListener flowListener,
			Chain operations,
			CompletionCallback<Object, Object> callback) {


		/**
		 * BEFORE scope logger
		 * ===================
		 **/

		Long initialTimestamp,loggerTimestamp;
		initialTimestamp = loggerTimestamp = System.currentTimeMillis();

		initLoggerCategory(category);

		logger.debug("correlationInfo.getEventId(): " + correlationInfo.getEventId());
		logger.debug("correlationInfo.getCorrelationId(): " + correlationInfo.getCorrelationId());
		logger.debug("transactionId: " + transactionId);

		try {
			// Add cache entry for initial timestamp based on unique EventId
			initialTimestamp = configs.getConfig(configurationRef).getCachedTimerTimestamp(transactionId, initialTimestamp);
		} catch (Exception e) {
			logger.error("initialTimestamp could not be retrieved from the cache config. Defaulting to current System.currentTimeMillis()", e);
		}

		// Calculate elapsed time based on cached initialTimestamp
		Long elapsed = loggerTimestamp - initialTimestamp;

		//config.printTimersKeys();
		if (elapsed == 0) {
			logger.debug("configuring flowListener....");
			flowListener.onComplete(new TimerRemoverRunnable(transactionId, configs.getConfig(configurationRef)));
		} else {
			logger.debug("flowListener already configured");
		}

		/**
		 * Avoid Logger Scope logic execution based on log priority
		 */
		if (isLogEnabled(priority.toString())) {
			// Execute Scope Logger
			ObjectNode loggerProcessor = om.createObjectNode();

			/**
			 * Custom field ordering for Logger Scope
			 * ===============================
			 **/
			loggerProcessor.put("correlationId", transactionId);
			loggerProcessor.put("flowName", flowName);
			loggerProcessor.put("tracePoint", scopeTracePoint.toString() + "_BEFORE");
			loggerProcessor.put("priority", priority.toString());
			loggerProcessor.put("elapsed", elapsed);
			loggerProcessor.put("scopeElapsed", 0);

			Map<String, String> locationInfoMap = locationInfoToMap(location);
			loggerProcessor.putPOJO("locationInfo", locationInfoMap);

			loggerProcessor.put("timestamp", getFormattedTimestamp(loggerTimestamp));
			loggerProcessor.put("applicationName", configs.getConfig(configurationRef).getApplicationName());
			loggerProcessor.put("environment", configs.getConfig(configurationRef).getApplicationEnvironment());
			loggerProcessor.put("threadName", Thread.currentThread().getName());

			// Define JSON output formatting
			// Print Logger
			String finalLogBefore = printObjectToLog(loggerProcessor, priority.toString(), true);

			// Added temp variable to comply with lambda
			Long finalInitialTimestamp = initialTimestamp;
			operations.process(
					result -> {

						/**
						 * AFTER scope logger
						 * ===================
						 **/

						Long endScopeTimestamp = System.currentTimeMillis();

						// Calculate elapsed time
						Long scopeElapsed = endScopeTimestamp - loggerTimestamp;
						Long mainElapsed = endScopeTimestamp - finalInitialTimestamp;

						loggerProcessor.put("flowName", flowName);
						loggerProcessor.put("tracePoint", scopeTracePoint.toString() + "_AFTER");
						loggerProcessor.put("priority", priority.toString());
						loggerProcessor.put("elapsed", mainElapsed);
						loggerProcessor.put("scopeElapsed", scopeElapsed);
						loggerProcessor.put("timestamp", getFormattedTimestamp(endScopeTimestamp));

						// Print Logger
						String finalLogAfter = printObjectToLog(loggerProcessor, priority.toString(), true);

						callback.success(result);
					},
					(error, previous) -> {

						/** ERROR scope logger **/

						Long errorScopeTimestamp = System.currentTimeMillis();
						Long mainElapsed = errorScopeTimestamp - finalInitialTimestamp;

						// Calculate elapsed time
						Long scopeElapsed = errorScopeTimestamp - loggerTimestamp;

						loggerProcessor.put("message", "Error found: " + error.getMessage());
						loggerProcessor.put("tracePoint", "EXCEPTION_SCOPE");
						loggerProcessor.put("flowName", flowName);
						loggerProcessor.put("priority", "ERROR");
						loggerProcessor.put("elapsed", mainElapsed);
						loggerProcessor.put("scopeElapsed", scopeElapsed);
						loggerProcessor.put("timestamp", getFormattedTimestamp(errorScopeTimestamp));

						// Print Logger
						String finalLogError = printObjectToLog(loggerProcessor, "ERROR", true);

						callback.error(error);
					});
		} else {
			// Execute operations without Logger
			logger.debug("Avoiding logger scope logic execution due to log priority not being enabled");
			operations.process(
					result -> {
						callback.success(result);
					},
					(error, previous) -> {
						callback.error(error);
					});
		}
	}

	protected void initLoggerCategory(String category) {
		if (category != null) {
			jsonLogger = LoggerFactory.getLogger(category);
		} else {
			jsonLogger = LoggerFactory.getLogger("org.liem.extension.logging.CustomLogger");
		}
		logger.debug("category set: " + jsonLogger.getName());
	}

	private Boolean isLogEnabled(String priority) {
		switch (priority) {
			case "TRACE":
				return jsonLogger.isTraceEnabled();
			case "DEBUG":
				return jsonLogger.isDebugEnabled();
			case "INFO":
				return jsonLogger.isInfoEnabled();
			case "WARN":
				return jsonLogger.isWarnEnabled();
			case "ERROR":
				return jsonLogger.isErrorEnabled();
		}
		return false;
	}

	private String printObjectToLog(ObjectNode loggerObj, String priority, boolean isPrettyPrint) {
	//ObjectMapper om = new ObjectMapper();
		ObjectWriter ow = (isPrettyPrint) ? om.writer().withDefaultPrettyPrinter() : om.writer();
		String logLine = "";
		try {
			logLine = ow.writeValueAsString(loggerObj);
		} catch (Exception e) {
			logger.error("Error parsing log data as a string", e);
		}
		doLog(priority.toString(), logLine);

		return logLine;
	}

	private void doLog(String priority, String logLine) {
		switch (priority) {
			case "TRACE":
				jsonLogger.trace(logLine);
				break;
			case "DEBUG":
				jsonLogger.debug(logLine);
				break;
			case "INFO":
				jsonLogger.info(logLine);
				break;
			case "WARN":
				jsonLogger.warn(logLine);
				break;
			case "ERROR":
				jsonLogger.error(logLine);
				break;
		}
	}

		private Map<String, String> locationInfoToMap(ComponentLocation location) {
			Map<String, String> locationInfo = new HashMap<String, String>();
			//locationInfo.put("location", location.getLocation());
			locationInfo.put("rootContainer", location.getRootContainerName());
			locationInfo.put("component", location.getComponentIdentifier().getIdentifier().toString());
			locationInfo.put("fileName", location.getFileName().orElse(""));
			locationInfo.put("lineInFile", String.valueOf(location.getLineInFile().orElse(null)));
			return locationInfo;
		}

	private String getFormattedTimestamp(Long loggerTimestamp) {
    /*
        Define timestamp:
        - DateTime: Defaults to ISO format
        - TimeZone: Defaults to UTC. Refer to https://en.wikipedia.org/wiki/List_of_tz_database_time_zones for valid timezones
    */
		DateTime dateTime = new DateTime(loggerTimestamp).withZone(org.joda.time.DateTimeZone.forID(System.getProperty("json.logger.timezone", "UTC")));
		String timestamp = dateTime.toString();
		if (System.getProperty("json.logger.dateformat") != null && !System.getProperty("json.logger.dateformat").equals("")) {
			timestamp = dateTime.toString(System.getProperty("json.logger.dateformat"));
		}
		return timestamp;
	}

	// Allows executing timer cleanup on flowListener onComplete events
	private static class TimerRemoverRunnable implements Runnable {

		private final String key;
		private final LoggingConfiguration config;

		public TimerRemoverRunnable(String key, LoggingConfiguration config) {
			this.key = key;
			this.config = config;
		}

		@Override
		public void run() {
			logger.debug("Removing key: " + key);
			config.removeCachedTimerTimestamp(key);
		}
	}

	/*private Object getData(InputStream payload, CommonAttributes commonAttributes) throws IOException {
		Object data;
		if (commonAttributes.getMimeType().contains(MediaType.APPLICATION_JSON)) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readValue(payload, JsonNode.class);
			if(jsonNode.isArray())
				data = mapper.convertValue(jsonNode, new TypeReference<List<Map<String, Object>>>(){});
			else
				data = mapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>(){});
		} else {
			data = new BufferedReader(
					new InputStreamReader(payload, StandardCharsets.UTF_8))
					.lines()
					.collect(Collectors.joining("\n"));
		}
		return data;
	}*/

	/*private void logLvlCheck(String data, String logLevel) {
		String definedLogLevel = logger.getLevel().toString();
		switch (logLevel) {
			case "INFO":
				if(definedLogLevel.equals(logLevel))
					logger.info(data);
				break;
			case "DEBUG":
				if(definedLogLevel.equals(logLevel))
					logger.debug(data);
				break;
			case "TRACE":
				if(definedLogLevel.equals(logLevel))
					logger.trace(data);
				break;
			case "ERROR":
				if(definedLogLevel.equals(logLevel))
					logger.error(data);
				break;
			case "WARN":
				if(definedLogLevel.equals(logLevel))
					logger.warn(data);
				break;
			default:
				throw new IllegalArgumentException("Unsupported log level: " + logLevel);
		}
	}*/
}
