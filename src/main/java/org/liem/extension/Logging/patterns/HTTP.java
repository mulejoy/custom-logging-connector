package org.liem.extension.logging.patterns;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class HTTP implements IntegrationPattern {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTP.class);
    ObjectMapper om = new ObjectMapper();

    @Parameter
    @Optional(defaultValue = "#[attributes.method]")
    @Summary("HTTP Request Method")
    @DisplayName("Request Method")
    private String uri;

    @Parameter
    @Optional(defaultValue = "#[attributes.relativePath default null]")
    @Summary("HTTP Resource Path")
    @DisplayName("Resource Path")
    private String resource;

    @Parameter
    @Optional(defaultValue = "#[attributes.headers['client_id'] default null]")
    @Summary("Client ID received as part of the HTTP Headers")
    @DisplayName("Client ID")
    private String clientId;

    @Parameter
    @Optional(defaultValue = "#[attributes.scheme default null]")
    @Summary("HTTP Request Scheme")
    @DisplayName("Scheme")
    private String scheme;

    @Parameter
    @Optional(defaultValue = "#[output application/json --- attributes.queryParams]")
    @Example("#[output application/json --- attributes.queryParams]")
    @NullSafe
    @Summary("Query Parameters as part of the HTTP Request")
    @DisplayName("Query Parameters")
    @Content(primary = true)
    private InputStream queryParameters;

    @Parameter
    @Optional(defaultValue = "#[output application/json --- attributes.uriParams]")
    @NullSafe
    @Summary("URI Parameters as part of the HTTP Request")
    @DisplayName("URI Parameters")
    @Content
    private InputStream uriParameters;

    @Parameter
    @Optional(defaultValue = "#[attributes.remoteAddress]")
    @Summary("Remote IP address")
    @DisplayName("Remote IP address")
    private String remoteAddress;

    @Parameter
    @Optional
    @Summary("HTTP Status")
    @DisplayName("Response Status")
    private String httpStatus;

    @Override
    public String getSelectedIntegrationPattern() {
        return "HTTP";
    }

    @Override
    public HashMap<String, Object> prepareData() throws IOException {
        HashMap<String,Object> data = new HashMap<>();
        data.put("pattern", this.getSelectedIntegrationPattern());
        data.put("uri", this.getUri());
        data.put("resource", this.getResource());
        data.put("clientId", this.getClientId());
        data.put("scheme", this.getScheme());
        data.put("queryParameters", this.getQueryParameters());
        data.put("uriParameters", this.getUriParameters());
        data.put("remoteAddress", this.getRemoteAddress());
        data.put("httpStatus", this.getHttpStatus());
        return data;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public InputStream getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(InputStream queryParameters) {
        this.queryParameters = queryParameters;
    }

    public InputStream getUriParameters() {
        return uriParameters;
    }

    public void setUriParameters(InputStream uriParameters) {
        this.uriParameters = uriParameters;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }
}
