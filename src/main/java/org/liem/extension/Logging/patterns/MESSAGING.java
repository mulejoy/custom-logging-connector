package org.liem.extension.logging.patterns;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class MESSAGING implements IntegrationPattern {

    private static final Logger LOGGER = LoggerFactory.getLogger(MESSAGING.class);
    ObjectMapper om = new ObjectMapper();

    @Parameter
    @Optional(defaultValue = "#[output application/json --- {}]")
    @DisplayName("Message Headers / Properties")
    @Content
    @Summary("Print the Message Headers / Properties")
    private InputStream msgHdrs;


    @Override
    public String getSelectedIntegrationPattern() {
        return "MESSAGING";
    }

    @Override
    public HashMap<String, Object> prepareData() throws IOException {
        HashMap<String,Object> data = new HashMap<>();
        data.put("pattern", this.getSelectedIntegrationPattern());
        data.put("msgHdrs", this.getMsgHdrs());
        return data;
    }

    public InputStream getMsgHdrs() {
        return msgHdrs;
    }

    public void setMsgHdrs(InputStream msgHdrs) {
        this.msgHdrs = msgHdrs;
    }
}
