package org.mulejoy.extension.logging.internal.singleton;

import org.mulejoy.extension.logging.internal.LoggingConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigsSingleton {

    private Map<String, LoggingConfiguration> configs = new HashMap<>();

    public Map<String, LoggingConfiguration> getConfigs() {
        return configs;
    }

    public LoggingConfiguration getConfig(String configName) {
        return this.configs.get(configName);
    }

    public void addConfig(String configName, LoggingConfiguration config) {
        this.configs.put(configName, config);
    }
}
