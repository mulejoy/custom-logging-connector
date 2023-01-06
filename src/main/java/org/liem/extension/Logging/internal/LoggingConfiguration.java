package org.liem.extension.logging.internal;

import org.liem.extension.logging.internal.singleton.ConfigsSingleton;
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

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations(LoggingOperations.class)
//@ConnectionProviders(LoggingConnectionProvider.class)
public class LoggingConfiguration implements Initialisable, Disposable {

  @Inject
  ConfigsSingleton configsSingleton;

  @RefName
  private String configName;

  public String getConfigName() {
    return configName;
  }

  /**
   * Application name
   */

  @Parameter
  @Optional(defaultValue="#[app.name]")
  @Summary("Name of the Mule application. Recommendation: This value should be based on pom.xml")
  private String applicationName;
  /**
   * Application Environment
   */
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

  /** Timer methods for Elapsed Time **/

  public ConcurrentHashMap<String,Long> timers = new ConcurrentHashMap<String,Long>();

  public ConcurrentHashMap<String, Long> getTimers() { return timers; }

  public void setTimers(ConcurrentHashMap<String, Long> timers) { this.timers = timers; }

  public void printTimersKeys () {
    System.out.println("Current timers: " + timers);
  }

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
    // Since SDK doesn't support passing of configs to Scopes, we have to explicitly create a Singleton bean and reference the global config
    configsSingleton.addConfig(configName, this);
  }
}
