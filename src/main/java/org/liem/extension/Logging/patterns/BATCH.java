package org.liem.extension.logging.patterns;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class BATCH implements IntegrationPattern {

    private static final Logger LOGGER = LoggerFactory.getLogger(BATCH.class);

    @Parameter
    @Optional(defaultValue = "#[vars.batchRecordId default \"N/A\"]")
    @DisplayName("Batch Record ID")
    @Summary("It tells us which record is being processed out of the batch the application currently handling. This needs to be set manually")
    private String batchRecordId;


    @Override
    public String getSelectedIntegrationPattern() {
        return "BATCH";
    }

    @Override
    public HashMap<String, Object> prepareData() {
        HashMap<String,Object> data = new HashMap<>();
        data.put("pattern", this.getSelectedIntegrationPattern());
        data.put("batchRecordId", this.getBatchRecordId());
        return data;
    }

    public String getBatchRecordId() {
        return batchRecordId;
    }

    public void setBatchRecordId(String batchRecordId) {
        this.batchRecordId = batchRecordId;
    }
}
