package com.openlib.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.qameta.allure.Step;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthorApiStepDefinitions {

    private static final String BASE_URL = "https://openlibrary.org";
    private static final String SCHEMA_PATH = "src/test/resources/schemas/author-schema.json";

    private ScenarioContext context;

    public AuthorApiStepDefinitions(ScenarioContext context) {
        this.context = context;
    }

    @Before
    public void beforeScenario() {
        context.clear();
        ScenarioContextHolder.setContext(context);
    }

    @After
    public void afterScenario() {
        // Log any test data for debugging
        System.out.println("Scenario completed. Test data: Author=" + context.getAuthorId() + 
            ", Status=" + context.getExpectedStatusCode());
    }

    @Given("the OpenLibrary API is available")
    @Step("Setup OpenLibrary API baseline")
    public void setupOpenLibraryApi() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        System.out.println("OpenLibrary API base URL configured: " + BASE_URL);
    }

    @Given("the OpenLibrary API is available with a defined schema")
    @Step("Setup OpenLibrary API with schema validation")
    public void setupOpenLibraryApiWithSchema() throws Exception {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        String schema = new String(Files.readAllBytes(Paths.get(SCHEMA_PATH)));
        context.setSchema(schema);
        System.out.println("OpenLibrary API and schema configured successfully");
    }

    @When("a GET request is made to fetch author {string}")
    @Step("Make GET request to fetch author {0}")
    public void makeGetRequestForAuthor(String authorIdentifier) {
        context.setAuthorId(authorIdentifier);
        String endpoint = "/authors/" + authorIdentifier + ".json";
        
        Response response = given()
                .header("Accept", "application/json")
                .when()
                .get(endpoint);
        
        context.setLastResponse(response);

        System.out.println("GET request made to endpoint: " + endpoint);
        System.out.println("Response Status Code: " + response.getStatusCode());
    }

    @When("two sequential GET requests are made to fetch author {string}")
    @Step("Make two sequential GET requests for author {0}")
    public void makeTwoSequentialGetRequests(String authorIdentifier) {
        // First request
        makeGetRequestForAuthor(authorIdentifier);
        
        // Second request
        String endpoint = "/authors/" + authorIdentifier + ".json";

        Response response = given()
                .header("Accept", "application/json")
                .when()
                .get(endpoint);
        
        context.setLastResponse(response);

        System.out.println("Two sequential GET requests completed for author: " + authorIdentifier);
    }

    @Then("the response status code should be {int}")
    @Step("Verify response status code is {0}")
    public void verifyResponseStatusCode(int expectedStatusCode) {
        context.setExpectedStatusCode(expectedStatusCode);
        Response response = context.getLastResponse();
        
        assertThat(response.getStatusCode())
                .as("Response status code should be " + expectedStatusCode)
                .isEqualTo(expectedStatusCode);

        response.then()
                .statusCode(expectedStatusCode);

        System.out.println("Response status code verified: " + expectedStatusCode);
    }

    @Then("the response should contain personal_name as {string}")
    @Step("Verify personal_name is {0}")
    public void verifyPersonalName(String expectedName) {
        context.setExpectedPersonalName(expectedName);
        Response response = context.getLastResponse();
        
        String actualName = response.jsonPath().getString("personal_name");
        
        assertThat(actualName)
                .as("Personal name should be '" + expectedName + "'")
                .isEqualTo(expectedName);

        response.then()
                .body("personal_name", equalTo(expectedName));

        System.out.println("Personal name verified: " + expectedName);
    }

    @Then("the response should contain alternate_names with {string}")
    @Step("Verify alternate_names contains {0}")
    public void verifyAlternateNames(String expectedAlternateName) {
        context.setExpectedAlternateName(expectedAlternateName);
        Response response = context.getLastResponse();
        
        List<String> alternateNames = response.jsonPath().getList("alternate_names");
        
        assertThat(alternateNames)
                .as("Alternate names should contain '" + expectedAlternateName + "'")
                .contains(expectedAlternateName);

        response.then()
                .body("alternate_names", hasItem(expectedAlternateName));

        System.out.println("Alternate name verified: " + expectedAlternateName);
    }

    @Then("the response content type should be {string}")
    @Step("Verify content type is {0}")
    public void verifyContentType(String expectedContentType) {
        context.setExpectedContentType(expectedContentType);
        Response response = context.getLastResponse();
        
        response.then()
                .contentType(expectedContentType);

        System.out.println("Content type verified: " + expectedContentType);
    }

    @Then("the response should validate against the author schema")
    @Step("Validate response against author schema")
    public void validateResponseAgainstSchema() {
        try {
            Response response = context.getLastResponse();
            String schema = context.getSchema();
            
            if (schema == null) {
                schema = new String(Files.readAllBytes(Paths.get(SCHEMA_PATH)));
                context.setSchema(schema);
            }

            response.then()
                    .body(matchesJsonSchema(schema));

            System.out.println("Response validated successfully against schema");
        } catch (Exception e) {
            System.err.println("Schema validation failed: " + e.getMessage());
            throw new RuntimeException("Schema validation error", e);
        }
    }

    @Then("the response should contain required fields {string} and {string}")
    @Step("Verify required fields {0} and {1}")
    public void verifyRequiredFields(String field1, String field2) {
        Response response = context.getLastResponse();
        
        response.then()
                .body(field1, notNullValue())
                .body(field2, notNullValue());

        String value1 = response.jsonPath().getString(field1);
        String value2 = response.jsonPath().getString(field2);

        assertThat(value1).isNotNull();
        assertThat(value2).isNotNull();

        System.out.println("Required fields verified: " + field1 + ", " + field2);
    }

    @Then("the response should contain the following fields:")
    @Step("Verify response contains specified fields")
    public void verifyResponseContainsFields(io.cucumber.datatable.DataTable dataTable) {
        List<String> fields = dataTable.asList();
        context.setExpectedFields(fields);
        Response response = context.getLastResponse();

        for (String field : fields) {
            Object value = response.jsonPath().get(field);
            assertThat(value)
                    .as("Field '" + field + "' should exist in response")
                    .isNotNull();
            System.out.println("Field verified: " + field);
        }
    }

    @Then("both responses should return status code {int}")
    @Step("Verify both responses have status code {0}")
    public void verifyBothResponsesHaveStatusCode(int expectedStatusCode) {
        context.setExpectedStatusCode(expectedStatusCode);
        Response currentResponse = context.getLastResponse();
        Response previousResponse = context.getPreviousResponse();
        
        assertThat(previousResponse.getStatusCode())
                .as("First response status code should be " + expectedStatusCode)
                .isEqualTo(expectedStatusCode);

        assertThat(currentResponse.getStatusCode())
                .as("Second response status code should be " + expectedStatusCode)
                .isEqualTo(expectedStatusCode);

        System.out.println("Both responses verified with status code: " + expectedStatusCode);
    }

    @Then("both responses should have the same personal_name")
    @Step("Verify both responses have the same personal_name")
    public void verifyBothResponsesSamePersonalName() {
        Response currentResponse = context.getLastResponse();
        Response previousResponse = context.getPreviousResponse();
        
        String firstName = previousResponse.jsonPath().getString("personal_name");
        String secondName = currentResponse.jsonPath().getString("personal_name");

        assertThat(firstName)
                .as("Personal names should be identical in both responses")
                .isEqualTo(secondName);
        
        context.setExpectedPersonalName(firstName);

        System.out.println("Personal name consistency verified: " + firstName);
    }

    @Then("both responses should have the same alternate_names")
    @Step("Verify both responses have the same alternate_names")
    public void verifyBothResponsesSameAlternateNames() {
        Response currentResponse = context.getLastResponse();
        Response previousResponse = context.getPreviousResponse();
        
        List<String> firstNames = previousResponse.jsonPath().getList("alternate_names");
        List<String> secondNames = currentResponse.jsonPath().getList("alternate_names");

        assertThat(firstNames)
                .as("Alternate names should be identical in both responses")
                .isEqualTo(secondNames);

        System.out.println("Alternate names consistency verified");
    }}