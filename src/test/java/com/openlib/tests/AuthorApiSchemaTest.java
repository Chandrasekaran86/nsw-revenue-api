package com.openlib.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.openlib.stepdefinitions.ScenarioContext;
import com.openlib.stepdefinitions.ScenarioContextHolder;

import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.hamcrest.Matchers.equalTo;

@Feature("OpenLibrary API Schema Validation")
@Story("Author Endpoint Schema Tests")
@DisplayName("OpenLibrary Author API Schema Validation Tests")
public class AuthorApiSchemaTest {

    private static final String BASE_URL = "https://openlibrary.org";
    private static final String AUTHOR_ENDPOINT = "/authors/OL1A.json";
    private static final String SCHEMA_PATH = "schemas/author-schema.json";
    
    // Expected values from feature file
    private static final int EXPECTED_STATUS_CODE = 200;
    private static final String EXPECTED_PERSONAL_NAME = "Sachi Rautroy";

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /**
     * Gets the expected value from the scenario context if available,
     * otherwise falls back to the predefined constant
     */
    private int getExpectedStatusCode() {
        ScenarioContext context = ScenarioContextHolder.getContext();
        if (context != null && context.getExpectedStatusCode() > 0) {
            return context.getExpectedStatusCode();
        }
        return EXPECTED_STATUS_CODE;
    }
    
    private String getExpectedPersonalName() {
        ScenarioContext context = ScenarioContextHolder.getContext();
        if (context != null && context.getExpectedPersonalName() != null) {
            return context.getExpectedPersonalName();
        }
        return EXPECTED_PERSONAL_NAME;
    }

    @Test
    @AllureId("TEST_004")
    @DisplayName("Test GET Author Endpoint - Validate Response Against Schema")
    @Description("GIVEN the OpenLibrary author endpoint is available with a defined schema " +
            "WHEN a GET request is made to fetch author OL1A " +
            "THEN the response should conform to the defined JSON schema")
    public void testAuthorEndpointResponseSchema() throws Exception {
        // GIVEN - Schema is loaded
        String schema = new String(Files.readAllBytes(Paths.get("src/test/resources/" + SCHEMA_PATH)));
        int expectedStatusCode = getExpectedStatusCode();
        
        System.out.println("Test Data - Status: " + expectedStatusCode);

        // WHEN - Making GET request to author endpoint
        // THEN - Verify response conforms to schema
        given()
                .header("Accept", "application/json")
                .when()
                .get(AUTHOR_ENDPOINT)
                .then()
                .statusCode(expectedStatusCode)
                .body(matchesJsonSchema(schema));
    }

    @Test
    @AllureId("TEST_005")
    @DisplayName("Test GET Author Endpoint - Complete Validation (Format, Schema, Content)")
    @Description("GIVEN the OpenLibrary author endpoint is available " +
            "WHEN a GET request is made to fetch author OL1A " +
            "THEN the response should be valid JSON, conform to schema, " +
            "AND contain expected author details from feature file")
    public void testAuthorEndpointCompleteValidation() throws Exception {
        // GIVEN - Schema is loaded
        String schema = new String(Files.readAllBytes(Paths.get("src/test/resources/" + SCHEMA_PATH)));
        int expectedStatusCode = getExpectedStatusCode();
        String expectedPersonalName = getExpectedPersonalName();

        System.out.println("Test Data - Status: " + expectedStatusCode + ", PersonalName: " + expectedPersonalName);

        // WHEN - Making GET request
        Response response = given()
                .header("Accept", "application/json")
                .when()
                .get(AUTHOR_ENDPOINT);

        // THEN - Verify all aspects
        response.then()
                .statusCode(expectedStatusCode)
                .body(matchesJsonSchema(schema))
                .body("personal_name", equalTo(expectedPersonalName));

        System.out.println("All validations passed!");
    }

    @Test
    @AllureId("TEST_006")
    @DisplayName("Test GET Author Endpoint - Schema Validation with Additional Assertions")
    @Description("GIVEN the OpenLibrary author endpoint is available " +
            "WHEN a GET request is made to fetch author OL1A " +
            "THEN validate schema and verify required fields are present")
    public void testAuthorEndpointSchemaWithFieldValidation() throws Exception {
        // GIVEN - Schema is prepared
        String schema = new String(Files.readAllBytes(Paths.get("src/test/resources/" + SCHEMA_PATH)));
        String endpoint = AUTHOR_ENDPOINT;
        int expectedStatusCode = getExpectedStatusCode();

        // WHEN & THEN - Execute request and validate
        given()
                .header("Accept", "application/json")
                .when()
                .get(endpoint)
                .then()
                .statusCode(expectedStatusCode)
                .body(matchesJsonSchema(schema))
                .body("key", org.hamcrest.Matchers.notNullValue())
                .body("personal_name", org.hamcrest.Matchers.notNullValue())
                .body("alternate_names", org.hamcrest.Matchers.notNullValue());
    }
}
