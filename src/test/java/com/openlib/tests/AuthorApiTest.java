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

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Feature("OpenLibrary API")
@Story("Author Endpoint Tests")
@DisplayName("OpenLibrary Author API Tests")
public class AuthorApiTest {

    private static final String BASE_URL = "https://openlibrary.org";
    private static final String AUTHOR_ENDPOINT = "/authors/OL1A.json";
    
    // Expected values from feature file
    private static final int EXPECTED_STATUS_CODE = 200;
    private static final String EXPECTED_PERSONAL_NAME = "Sachi Rautroy";
    private static final String EXPECTED_ALTERNATE_NAME = "Yugashrashta Sachi Routray";
    private static final String EXPECTED_CONTENT_TYPE = "application/json";

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /**
     * Gets the expected value from the scenario context if available,
     * otherwise falls back to the predefined constant
     */
    private String getExpectedPersonalName() {
        ScenarioContext context = ScenarioContextHolder.getContext();
        if (context != null && context.getExpectedPersonalName() != null) {
            return context.getExpectedPersonalName();
        }
        return EXPECTED_PERSONAL_NAME;
    }
    
    private String getExpectedAlternateName() {
        ScenarioContext context = ScenarioContextHolder.getContext();
        if (context != null && context.getExpectedAlternateName() != null) {
            return context.getExpectedAlternateName();
        }
        return EXPECTED_ALTERNATE_NAME;
    }
    
    private int getExpectedStatusCode() {
        ScenarioContext context = ScenarioContextHolder.getContext();
        if (context != null && context.getExpectedStatusCode() > 0) {
            return context.getExpectedStatusCode();
        }
        return EXPECTED_STATUS_CODE;
    }
    
    private String getExpectedContentType() {
        ScenarioContext context = ScenarioContextHolder.getContext();
        if (context != null && context.getExpectedContentType() != null) {
            return context.getExpectedContentType();
        }
        return EXPECTED_CONTENT_TYPE;
    }

    @Test
    @AllureId("TEST_001")
    @DisplayName("Test GET Author Endpoint - Verify Response Code and Author Details")
    @Description("GIVEN the OpenLibrary author endpoint is available " +
            "WHEN a GET request is made to fetch author OL1A " +
            "THEN the response status code should be 200 " +
            "AND personal_name should be obtained from feature file " +
            "AND alternate_names should contain value from feature file")
    public void testGetAuthorEndpoint() {
        // GIVEN - Author endpoint is available
        String endpoint = AUTHOR_ENDPOINT;
        int expectedStatusCode = getExpectedStatusCode();
        String expectedPersonalName = getExpectedPersonalName();
        String expectedAlternateName = getExpectedAlternateName();

        System.out.println("Test Data - Status: " + expectedStatusCode + ", PersonalName: " + 
            expectedPersonalName + ", AlternateName: " + expectedAlternateName);

        // WHEN - Making GET request to author endpoint
        Response response = given()
                .header("Accept", "application/json")
                .when()
                .get(endpoint);

        // THEN - Verify response code
        response.then()
                .statusCode(expectedStatusCode);

        // Extract and log the response
        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);

        // Verify personal_name
        response.then()
                .body("personal_name", equalTo(expectedPersonalName));

        // Verify alternate_names contains the expected value
        response.then()
                .body("alternate_names", hasItem(expectedAlternateName));

        // Additional assertions using AssertJ
        String personalName = response.jsonPath().getString("personal_name");
        assertThat(personalName)
                .as("Personal name should match expected value from feature file")
                .isEqualTo(expectedPersonalName);

        java.util.List<String> alternateNames = response.jsonPath().getList("alternate_names");
        assertThat(alternateNames)
                .as("Alternate names should contain expected name from feature file")
                .contains(expectedAlternateName);

        // Verify response code
        assertThat(response.getStatusCode())
                .as("Response status code should match expected value from feature file")
                .isEqualTo(expectedStatusCode);
    }

    @Test
    @AllureId("TEST_002")
    @DisplayName("Test GET Author Endpoint - Verify Content Type")
    @Description("GIVEN the OpenLibrary author endpoint is available " +
            "WHEN a GET request is made to fetch author OL1A " +
            "THEN the response content type should be obtained from feature file")
    public void testAuthorEndpointContentType() {
        // GIVEN - Author endpoint is available
        String endpoint = AUTHOR_ENDPOINT;
        int expectedStatusCode = getExpectedStatusCode();
        String expectedContentType = getExpectedContentType();

        // WHEN & THEN - Verify content type
        given()
                .header("Accept", "application/json")
                .when()
                .get(endpoint)
                .then()
                .statusCode(expectedStatusCode)
                .contentType(expectedContentType);
    }

    @Test
    @AllureId("TEST_003")
    @DisplayName("Test GET Author Endpoint - Verify Response Contains Expected Fields")
    @Description("GIVEN the OpenLibrary author endpoint is available " +
            "WHEN a GET request is made to fetch author OL1A " +
            "THEN the response should contain required fields")
    public void testAuthorEndpointFieldPresence() {
        // GIVEN - Author endpoint is available
        String endpoint = AUTHOR_ENDPOINT;

        // WHEN & THEN - Verify response contains expected fields
        given()
                .header("Accept", "application/json")
                .when()
                .get(endpoint)
                .then()
                .statusCode(200)
                .body("personal_name", notNullValue())
                .body("alternate_names", notNullValue())
                .body("key", notNullValue());
    }
}
