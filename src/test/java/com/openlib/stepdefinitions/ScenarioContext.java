package com.openlib.stepdefinitions;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Shared context for storing test data across step definitions and tests
 * This contains all expected values from the feature file to avoid hardcoding
 */
public class ScenarioContext {
    
    private Response lastResponse;
    private Response previousResponse;
    private int expectedStatusCode;
    private String expectedPersonalName;
    private String expectedAlternateName;
    private String expectedContentType;
    private String authorId;
    private String schema;
    private List<String> expectedFields;
    private Map<String, Object> testData;
    
    public ScenarioContext() {
        this.testData = new HashMap<>();
    }
    
    // Response methods
    public Response getLastResponse() {
        return lastResponse;
    }
    
    public void setLastResponse(Response response) {
        this.previousResponse = this.lastResponse;
        this.lastResponse = response;
    }
    
    public Response getPreviousResponse() {
        return previousResponse;
    }
    
    // Expected values from feature file
    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }
    
    public void setExpectedStatusCode(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }
    
    public String getExpectedPersonalName() {
        return expectedPersonalName;
    }
    
    public void setExpectedPersonalName(String expectedPersonalName) {
        this.expectedPersonalName = expectedPersonalName;
    }
    
    public String getExpectedAlternateName() {
        return expectedAlternateName;
    }
    
    public void setExpectedAlternateName(String expectedAlternateName) {
        this.expectedAlternateName = expectedAlternateName;
    }
    
    public String getExpectedContentType() {
        return expectedContentType;
    }
    
    public void setExpectedContentType(String expectedContentType) {
        this.expectedContentType = expectedContentType;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public List<String> getExpectedFields() {
        return expectedFields;
    }
    
    public void setExpectedFields(List<String> expectedFields) {
        this.expectedFields = expectedFields;
    }
    
    // Generic test data storage
    public void put(String key, Object value) {
        testData.put(key, value);
    }
    
    public Object get(String key) {
        return testData.get(key);
    }
    
    public void clear() {
        testData.clear();
        lastResponse = null;
        previousResponse = null;
    }
}
