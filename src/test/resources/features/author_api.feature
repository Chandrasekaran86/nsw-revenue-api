Feature: OpenLibrary Author API Testing
  As a QA Engineer
  I want to test the OpenLibrary Author API endpoint
  So that I can validate the API response structure and content

  Scenario: Verify Author Endpoint Returns Expected Author Details
    Given the OpenLibrary API is available
    When a GET request is made to fetch author "OL1A"
    Then the response status code should be 200
    And the response should contain personal_name as "Sachi Rautroy"
    And the response should contain alternate_names with "Yugashrashta Sachi Routray"
    And the response content type should be "application/json"

  Scenario: Validate Author Response Against JSON Schema
    Given the OpenLibrary API is available with a defined schema
    When a GET request is made to fetch author "OL1A"
    Then the response status code should be 200
    And the response should validate against the author schema
    And the response should contain required fields "key" and "personal_name"

  Scenario: Verify Author Endpoint Response Contains Expected Fields
    Given the OpenLibrary API is available
    When a GET request is made to fetch author "OL1A"
    Then the response should contain the following fields:
      | key              |
      | personal_name    |
      | alternate_names  |
      | name             |

  Scenario: Verify Author Endpoint Consistency
    Given the OpenLibrary API is available
    When two sequential GET requests are made to fetch author "OL1A"
    Then both responses should return status code 200
    And both responses should have the same personal_name
    And both responses should have the same alternate_names
