package org.mulejoy.extension.logging.internal;

import org.mulejoy.extension.logging.internal.singleton.ConfigsSingleton;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;

@Operations(LoggingOperations.class)
public class LoggingConfiguration implements Initialisable, Disposable {

  @Inject
  ConfigsSingleton configsSingleton;

  @RefName
  private String configName;

  public String getConfigName() {
    return configName;
  }

  @Parameter
  @Optional(defaultValue="#[app.name]")
  @Summary("Name of the Mule application. Recommendation: This value should be based on pom.xml")
  private String applicationName;

  @Parameter
  @Example("${mule.env}")
  @Summary("Name of the Mule Environment where the application is running. Recommendation: This value should be based on external property")
  private String applicationEnvironment;

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getApplicationEnvironment() {
    return applicationEnvironment;
  }

  public void setApplicationEnvironment(String applicationEnvironment) {
    this.applicationEnvironment = applicationEnvironment;
  }

  public ConcurrentHashMap<String,Long> timers = new ConcurrentHashMap<String,Long>();

  public ConcurrentHashMap<String, Long> getTimers() { return timers; }

  public void setTimers(ConcurrentHashMap<String, Long> timers) { this.timers = timers; }

  public Long getCachedTimerTimestamp(String key, Long initialTimeStamp) throws Exception {
    Long startTimestamp = timers.putIfAbsent(key, initialTimeStamp);
    return (startTimestamp == null) ? timers.get(key) : startTimestamp;
  }

  public void removeCachedTimerTimestamp(String key) {
    timers.remove(key);
  }

  @Override
  public void dispose() {

  }

  @Override
  public void initialise() throws InitialisationException {
    // SDK doesn't support passing configs to Scopes — singleton bridges the gap
    configsSingleton.addConfig(configName, this);
  }
}
