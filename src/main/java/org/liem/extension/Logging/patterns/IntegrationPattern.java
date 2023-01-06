package org.liem.extension.logging.patterns;

import java.io.IOException;
import java.util.HashMap;

public interface IntegrationPattern {

    public String getSelectedIntegrationPattern();

    public HashMap<String, Object> prepareData() throws IOException;
}
