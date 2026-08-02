package org.mulejoy.extension.logging.patterns;

import java.io.IOException;
import java.util.HashMap;

public interface IntegrationPattern {

    String getSelectedIntegrationPattern();

    HashMap<String, Object> prepareData() throws IOException;
}
