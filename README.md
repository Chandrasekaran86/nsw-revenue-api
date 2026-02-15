# 🚀 OpenLibrary API Test Automation Framework

> A comprehensive, integration-focused API testing framework built with **Cucumber BDD**, **JUnit 5**, and **REST-Assured**. Feature files serve as the single source of truth for test data across both BDD and unit tests.

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Framework Design](#-framework-design)
- [ScenarioContext Pattern](#-scenariocontext-pattern)
- [Project Structure](#-project-structure)
- [Feature Files as Single Source of Truth](#-feature-files-as-single-source-of-truth)
- [Parallel Test Execution](#-parallel-test-execution)
- [Local Test Execution](#-local-test-execution)
- [GitHub Actions CI/CD](#-github-actions-cicd)
- [Test Data Flow Sequence Diagram](#-test-data-flow-sequence-diagram)
- [Technologies & Dependencies](#-technologies--dependencies)
- [Getting Started](#-getting-started)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Overview

This framework implements a **unified testing approach** where:
- ✅ **Feature files** define test scenarios and expected values
- ✅ **Step definitions** extract data from feature files into the `ScenarioContext`
- ✅ **JUnit tests** read shared test data from the context
- ✅ **Thread-local storage** ensures parallel execution safety
- ✅ **No hardcoded assertions** - all values come from feature files

### Key Benefits
| Feature | Benefit |
|---------|---------|
| 🔗 Single Source of Truth | One place to update test data |
| 🔄 Parallel Execution | Thread-safe via `ScenarioContextHolder` |
| 🎭 BDD + Unit Integration | Run tests independently or together |
| 📊 Comprehensive Reporting | Allure reports with step details |
| 🔄 Maintainability | Easy to update test scenarios |

---

## 🏗️ Architecture

### High-Level Design

```
┌─────────────────────────────────────────────────────────────┐
│                    Feature Files (YAML/Gherkin)             │
│                    Single Source of Truth                   │
│  ├── Status Codes (200, 400, etc.)                          │
│  ├── Expected Values (personal_name, alternate_names)       │
│  └── Field Validations                                      │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
   ┌─────────────┐ ┌──────────────┐ ┌──────────────┐
   │ Step Defs   │ │  JUnit Tests │ │ JSON Schema  │
   │ (Cucumber)  │ │              │ │ Validation   │
   └─────────────┘ └──────────────┘ └──────────────┘
        │                │                │
        └────────────────┼────────────────┘
                         │
        ┌────────────────▼────────────────┐
        │    ScenarioContext              │
        │  ├── expectedStatusCode         │
        │  ├── expectedPersonalName       │
        │  ├── expectedAlternateNames     │
        │  ├── lastResponse               │
        │  └── previousResponse           │
        └────────────────┬────────────────┘
                         │
        ┌────────────────▼────────────────┐
        │  ScenarioContextHolder          │
        │  (ThreadLocal<Context>)         │
        │  ├── Thread 1 → Context 1       │
        │  ├── Thread 2 → Context 2       │
        │  └── Thread N → Context N       │
        └─────────────────────────────────┘
```

---

## 📦 Framework Design

### 1️⃣ ScenarioContext Pattern

The `ScenarioContext` class acts as a **shared data container** for test execution:

#### Key Responsibilities:
- **Store Expected Values**: Status codes, assertions, names
- **Store Response Objects**: Last and previous HTTP responses
- **Provide Generic Storage**: Key-value pairs for custom data

#### Implementation

```java
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
    
    // Getters and setters for all fields
    public Response getLastResponse() { ... }
    public void setLastResponse(Response response) { ... }
    // ... more methods
}
```

### 2️⃣ ScenarioContextHolder (Thread-Local Pattern)

The `ScenarioContextHolder` provides **thread-safe** access to context:

```java
public class ScenarioContextHolder {
    private static final ThreadLocal<ScenarioContext> contextHolder = 
        ThreadLocal.withInitial(ScenarioContext::new);
    
    public static ScenarioContext getContext() {
        return contextHolder.get();
    }
    
    public static void setContext(ScenarioContext context) {
        contextHolder.set(context);
    }
    
    public static void clearContext() {
        contextHolder.remove();
    }
}
```

#### Thread-Local Benefits for Parallel Execution

| Scenario | Behavior |
|----------|----------|
| **Thread 1 Scenario A** | Has own isolated Context A |
| **Thread 2 Scenario B** | Has own isolated Context B |
| **Thread 3 Test 1** | Has own isolated Context 1 |
| **Thread 4 Test 2** | Has own isolated Context 2 |
| **No Interference** | Each thread operates independently |

---

## 📁 Project Structure

```
nsw-revenue-api/
│
├── .github/
│   └── workflows/
│       └── run-tests.yml                   🔄 GitHub Actions workflow
│
├── src/test/
│   ├── java/com/openlib/
│   │   ├── tests/                          📝 JUnit Test Classes
│   │   │   ├── AuthorApiTest.java          (Unit test - reads from context)
│   │   │   ├── AuthorApiSchemaTest.java    (Schema validation tests)
│   │   │   └── CucumberTestRunner.java     (Cucumber test runner)
│   │   │
│   │   └── stepdefinitions/                🔗 Cucumber Integration Layer
│   │       ├── AuthorApiStepDefinitions.java  (Extracts data → Context)
│   │       ├── ScenarioContext.java           (Data container)
│   │       └── ScenarioContextHolder.java     (Thread-local accessor)
│   │
│   └── resources/
│       ├── features/
│       │   └── author_api.feature          📖 Single Source of Truth
│       ├── schemas/
│       │   └── author-schema.json          (JSON Schema validation)
│       └── allure.properties               (Allure configuration)
│
├── pom.xml                                  📦 Maven configuration
├── README.md                                (This file)
└── setup.ps1 / setup.bat                    ⚙️ Setup scripts
```

### Folder Purposes

| Folder | Purpose | Owner |
|--------|---------|-------|
| `.github/workflows/` | CI/CD automation | DevOps |
| `tests/` | JUnit unit tests | QA Developers |
| `stepdefinitions/` | Cucumber step implementations | QA Developers |
| `features/` | Test scenarios in Gherkin | QA/Business Analysts |
| `schemas/` | API contract definitions | QA/API Design |
| `resources/` | Test configuration & data | QA |

---

## 🎯 Feature Files as Single Source of Truth

### Philosophy
All test data is **declared once** in the feature file and **referenced everywhere**:

```gherkin
# ✅ Feature file defines expected values
Feature: OpenLibrary Author API Testing

  Scenario: Verify Author Endpoint Returns Expected Author Details
    Given the OpenLibrary API is available
    When a GET request is made to fetch author "OL1A"
    Then the response status code should be 200
    And the response should contain personal_name as "Sachi Rautroy"
    And the response should contain alternate_names with "Yugashrashta Sachi Routray"
    And the response content type should be "application/json"
```

### Data Flow in Step Definitions

```java
// Step Definition extracts value from feature file
@Then("the response should contain personal_name as {string}")
@Step("Verify personal_name is {0}")
public void verifyPersonalName(String expectedName) {
    // ✅ expectedName = "Sachi Rautroy" (from feature file)
    
    context.setExpectedPersonalName(expectedName);  // Store in context
    Response response = context.getLastResponse();
    
    String actualName = response.jsonPath().getString("personal_name");
    assertThat(actualName).isEqualTo(expectedName);  // Use from context
}
```

### Data Flow in JUnit Tests

```java
// JUnit test retrieves value from shared context
@Test
public void testGetAuthorEndpoint() {
    String expectedPersonalName = getExpectedPersonalName();  // Get from context
    // ✅ Returns "Sachi Rautroy" that was set by step definition
    
    Response response = given()
        .header("Accept", "application/json")
        .when()
        .get(AUTHOR_ENDPOINT);
    
    response.then()
        .body("personal_name", equalTo(expectedPersonalName));  // Assert with context value
}
```

### Helper Method in JUnit Tests

```java
private String getExpectedPersonalName() {
    ScenarioContext context = ScenarioContextHolder.getContext();
    
    // Priority 1: Check if context has value from feature file
    if (context != null && context.getExpectedPersonalName() != null) {
        return context.getExpectedPersonalName();
    }
    
    // Priority 2: Fall back to test constant (for standalone execution)
    return EXPECTED_PERSONAL_NAME;
}
```

### Advantages

✅ **Single Point of Change**: Update feature file, all tests use new value  
✅ **Reduced Duplication**: No repeated values across files  
✅ **Easier Maintenance**: Clear where test data comes from  
✅ **BDD Alignment**: Non-technical stakeholders can update values  
✅ **Type Safety**: Compile-time checking via constants  

---

## 🔄 Parallel Test Execution

### Parallel Execution Strategy

The framework supports running tests in parallel using Maven with **thread-local context isolation**:

#### Configuration for Parallel Execution

```xml
<!-- In pom.xml -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M9</version>
    <configuration>
        <!-- Enable parallel execution -->
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
        
        <!-- Or use -->
        <parallel>suites</parallel>
        <threadCount>2</threadCount>
    </configuration>
</plugin>
```

#### How ScenarioContextHolder Ensures Thread Safety

```
Main Process
│
├── Thread-1 (Scenario A)
│   └── ThreadLocal → ScenarioContext-A
│       ├── expectedStatusCode = 200
│       ├── expectedPersonalName = "Author A"
│       └── lastResponse = Response-A
│
├── Thread-2 (Test B)  
│   └── ThreadLocal → ScenarioContext-B
│       ├── expectedStatusCode = 200
│       ├── expectedPersonalName = "Author B"
│       └── lastResponse = Response-B
│
└── Thread-3 (Scenario C)
    └── ThreadLocal → ScenarioContext-C
        ├── expectedStatusCode = 200
        ├── expectedPersonalName = "Author C"
        └── lastResponse = Response-C

✅ No Data Cross-Contamination
✅ Each thread has isolated context
✅ Thread-safe assertions
```

#### Execution Commands

```bash
# Parallel by methods (8 threads)
mvn test -DthreadCount=8 -DparallelMethods=true

# Parallel by suites (4 threads)
mvn test -DthreadCount=4 -DparallelSuites=true

# Sequential (default)
mvn test
```

### ThreadLocal Implementation Details

```java
// ✅ Each thread gets its own instance
private static final ThreadLocal<ScenarioContext> contextHolder = 
    ThreadLocal.withInitial(ScenarioContext::new);

// Thread 1 calls getContext()
// ↓ Gets unique ScenarioContext instance for Thread 1
// ↓ Stored in Thread 1's ThreadLocal map

// Thread 2 calls getContext()  
// ↓ Gets unique ScenarioContext instance for Thread 2
// ↓ Stored in Thread 2's ThreadLocal map

// Thread 1 modifies expectedStatusCode = 200
// ✅ Thread 2's expectedStatusCode remains unchanged
```

---

## 🖥️ Local Test Execution

### Prerequisites

- ☕ **Java 11+** 
- 🔨 **Maven 3.6+**
- 🌐 **Internet connection** (for OpenLibrary API)

### Setup Steps

#### 1️⃣ Clone or Navigate to Project

```bash
cd nsw-revenue-api
```

#### 2️⃣ Install Dependencies

```bash
mvn clean install
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45.123 s
```

### Running Tests Locally

#### Run All Tests
```bash
mvn clean test
```

#### Run Only Cucumber Feature Tests
```bash
mvn test -Dtest=CucumberTestRunner
```

#### Run Only JUnit Tests
```bash
mvn test -Dtest=AuthorApiTest,AuthorApiSchemaTest
```

#### Run Specific Test Class
```bash
mvn test -Dtest=AuthorApiTest
```

#### Run Specific Test Method
```bash
mvn test -Dtest=AuthorApiTest#testGetAuthorEndpoint
```

#### Run Tests in Parallel (Local)
```bash
mvn test -DthreadCount=4 -DparallelMethods=true
```

### Generating Reports Locally

#### Step 1: Run Tests
```bash
mvn clean test
```

#### Step 2: Generate Allure Report
```bash
mvn allure:report
```

#### Step 3: Serve Allure Report
```bash
mvn allure:serve
```

**Result:** 🎉 Opens http://localhost:4040 in your browser with interactive report

### Test Execution Flow (Local)

```
1. mvn clean test
   ↓
2. Maven loads pom.xml
   ├── Resolves dependencies
   ├── Compiles Java code
   └── Copies resources (feature files, schemas)
   ↓
3. Test Execution Begins
   ├── CucumberTestRunner.java loaded
   ├── author_api.feature scenarios read
   ├── AuthorApiStepDefinitions instantiated
   │   └── ScenarioContext injected via constructor
   │   └── ScenarioContextHolder.setContext() called in @Before
   │
   ├── For each Scenario:
   │   ├── @Before → Initialize context
   │   ├── Scenario executes
   │   │   ├── GIVEN step → setupOpenLibraryApi()
   │   │   ├── WHEN step → makeGetRequestForAuthor("OL1A")
   │   │   │                └── context.setLastResponse(response)
   │   │   └── THEN steps → verifyResponseStatusCode(200)
   │   │                     └── context.setExpectedStatusCode(200)
   │   │
   │   ├── AuthorApiTest.testGetAuthorEndpoint() can now run
   │   │   └── getExpectedStatusCode() reads from context
   │   │
   │   └── @After → context.clear()
   │
   ├── AuthorApiSchemaTest runs independently
   │   └── Falls back to EXPECTED_STATUS_CODE constant
   │
   └── All tests completed
   ↓
4. Test Results
   ├── target/surefire-reports/ (XML/HTML summaries)
   ├── target/allure-results/ (JSON for Allure)
   └── Console output (PASS/FAIL summary)
   ↓
5. Report Generation (optional)
   └── mvn allure:report → target/site/allure-maven-plugin/
```

---

## 🚀 GitHub Actions CI/CD

### Workflow Features

The `.github/workflows/run-tests.yml` automates:
- ✅ Automatic test execution on trigger
- ✅ Java 11 + Maven setup
- ✅ Dependency caching (faster builds)
- ✅ Test result collection
- ✅ Allure report generation
- ✅ Artifact uploads (30-day retention)

### Step-by-Step GitHub Actions Execution

#### Step 1: View Workflow File

```yaml
name: Run API Tests
on:
  workflow_dispatch:  # ← Manual trigger

jobs:
  test:
    runs-on: ubuntu-latest  # ← Ubuntu environment
```

#### Step 2: Trigger Workflow

1. **Go to Repository**
   ```
   https://github.com/YOUR_ORG/nsw-revenue-api
   ```

2. **Navigate to Actions Tab**
   ```
   Click "Actions" → Select "Run API Tests"
   ```

3. **Trigger Workflow**
   ```
   Click "Run workflow" → Select branch → Click "Run workflow"
   ```

#### Step 3: Workflow Execution Timeline

```
1. Checkout Repository (5s)
   ├── Clone repo at specific commit
   └── Fetch all history
   ↓
2. Set up OpenJDK 11 (45s)
   ├── Install Java 11 (Temurin)
   ├── Cache Maven dependencies
   └── Verify installation
   ↓
3. Verify Java Installation (2s)
   ├── java -version
   └── javac -version
   ↓
4. Verify Maven Installation (2s)
   └── mvn -version
   ↓
5. Build Project (30s)
   ├── mvn clean compile -DskipTests
   ├── No tests executed yet
   └── Verify compilation succeeds
   ↓
6. Run Tests (60s) ⭐ MAIN EXECUTION
   ├── mvn clean test
   ├── Compile test code
   ├── Execute all tests (Cucumber + JUnit)
   ├── Generate allure-results/
   └── Upload results
   ↓
7. Test Results Summary (2s)
   ├── Echo completion message
   └── Check pass/fail status
   ↓
8. Generate Allure Report (15s)
   ├── mvn allure:report
   ├── Create HTML report
   └── Prepare for upload
   ↓
9. Upload Artifacts (10s)
   ├── Test Results
   │   └── target/surefire-reports/ (XML)
   ├── Allure Results
   │   └── target/allure-results/ (JSON)
   └── Allure Report
       └── target/site/allure-maven-plugin/ (HTML)
   ↓
10. Completion
    ├── Success: "All tests passed!" ✅
    └── Failure: Check artifacts for logs ❌
```

### Viewing Workflow Runs

1. **Go to Actions Tab**
   ```
   https://github.com/YOUR_ORG/nsw-revenue-api/actions
   ```

2. **Select Latest Run**
   ```
   Click on "Run API Tests" workflow run
   ```

3. **View Execution Details**
   ```
   ├── Summary tab → Overall results
   ├── Logs → Step-by-step execution
   └── Artifacts → Download reports
   ```

### Downloading Test Artifacts

#### From the UI:

1. Click the workflow run
2. Scroll to "Artifacts" section
3. Download desired artifact:
   - `test-results` (XML summaries)
   - `allure-results` (JSON data)
   - `allure-report` (HTML report)

#### Using GitHub CLI:

```bash
# List artifacts
gh run list --repo YOUR_ORG/nsw-revenue-api

# Download artifact
gh run download <RUN_ID> \
  -n allure-report \
  -D ./reports

# View locally
open ./reports/index.html
```

### Workflow Configuration

#### Modify Trigger

To add push trigger:

```yaml
on:
  workflow_dispatch:  # ← Manual trigger
  push:               # ← Add automated trigger
    branches:
      - main
      - develop
```

#### Modify Java Version

```yaml
- name: Set up OpenJDK 11
  uses: actions/setup-java@v4
  with:
    java-version: '11'    # ← Change here (17, 21, etc.)
    distribution: 'temurin'
```

#### Modify Test Command

```yaml
- name: Run Tests
  run: mvn clean test -DthreadCount=8  # ← Add options
```

---

## 📊 Test Data Flow Sequence Diagram

The diagram below shows how data flows from feature files through the system:

```
Feature File                  Step Definitions          ScenarioContext        JUnit Tests
─────────────────────────────────────────────────────────────────────────────────────────

Feature Execution
│
├── Scenario Starts
│   │
│   ├──────────────────→ @Before Hook
│   │                   ├── Create ScenarioContext
│   │                   └── ScenarioContextHolder.setContext(context)
│   │
│   ├── GIVEN Step
│   │  "API is available"
│   │   │
│   │   ├──────────────→ setupOpenLibraryApi()
│   │   │               └── RestAssured.baseURI = "https://openlibrary.org"
│   │   │
│   │   └──────────────→ (return)
│   │
│   ├── WHEN Step  
│   │  "GET author OL1A"
│   │   │
│   │   ├──────────────→ makeGetRequestForAuthor("OL1A")
│   │   │               ├── Execute HTTP GET
│   │   │               ├── Receive Response
│   │   │               └── context.setLastResponse(response)
│   │   │                   └── Store in context ✓
│   │   │
│   │   └──────────────→ (return)
│   │
│   ├── THEN Step
│   │  "status code 200"
│   │   │
│   │   ├──────────────→ verifyResponseStatusCode(200)
│   │   │               ├── context.setExpectedStatusCode(200)
│   │   │               │   └── Store in context ✓
│   │   │               └── response.then().statusCode(200)
│   │   │                   └── Assertion ✓
│   │   │
│   │   ├──────────────→ (return)
│   │
│   ├── THEN Step
│   │  "personal_name 'Sachi Rautroy'"
│   │   │
│   │   ├──────────────→ verifyPersonalName("Sachi Rautroy")
│   │   │               ├── context.setExpectedPersonalName("Sachi Rautroy")
│   │   │               │   └── Store in context ✓
│   │   │               └── Assert against response
│   │   │
│   │   └──────────────→ (return)
│   │
│   └──────────────────→ @After Hook
│                       └── context.clear()
│
Scenario Complete ✓
│
                                                          JUnit Test Execution
                                                          ─────────────────────
                                                          │
                                                          ├── @Test testGetAuthorEndpoint()
                                                          │   │
                                                          │   ├─ getExpectedStatusCode()
                                                          │   │  │
                                                          │   │  └─→ ScenarioContextHolder.getContext()
                                                          │   │      ├── ThreadLocal retrieval
                                                          │   │      └── Returns 200 ✓
                                                          │   │
                                                          │   ├─ getExpectedPersonalName()
                                                          │   │  │
                                                          │   │  └─→ ScenarioContextHolder.getContext()
                                                          │   │      ├── ThreadLocal retrieval
                                                          │   │      └── Returns "Sachi Rautroy" ✓
                                                          │   │
                                                          │   ├─ Execute API request
                                                          │   │   └── GET /authors/OL1A.json
                                                          │   │
                                                          │   └─ Assert with context values
                                                          │       ├── response.statusCode == 200 ✓
                                                          │       └── response.personalName == "Sachi Rautroy" ✓
                                                          │
                                                          └── Test PASSED ✓

Legend:
✓ = Successfully stored/retrieved from context
→ = Method call
─ = Flow direction
```

---

## 🛠️ Technologies & Dependencies

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 11+ | Programming language |
| **Maven** | 3.6+ | Build and dependency management |
| **REST-Assured** | 5.3.1 | REST API testing library |
| **JUnit 5** | 5.9.2 | Testing framework |
| **Cucumber** | 7.13.0 | BDD test execution |
| **Allure** | 2.23.0 | Test reporting & analytics |
| **AssertJ** | 3.24.1 | Fluent assertions |
| **Hamcrest** | 2.2+ | Matcher library |
| **SLF4J** | 2.0.7 | Logging framework |

### Maven Configuration (pom.xml)

Key dependency management:

```xml
<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <cucumber.version>7.13.0</cucumber.version>
    <allure.version>2.23.0</allure.version>
    <junit.version>5.9.2</junit.version>
    <rest-assured.version>5.3.1</rest-assured.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- Explicitly manage Cucumber Messages to resolve version conflicts -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>messages</artifactId>
            <version>22.0.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## 🚀 Getting Started

### Prerequisites

- ☕ Java 11 or higher
- 🔨 Maven 3.6 or higher
- 📀 Git (for version control)
- 🌐 Internet connection (to access OpenLibrary API)

### Installation & Setup

#### 1️⃣ Clone/Navigate to Repository

```bash
cd nsw-revenue-api
```

#### 2️⃣ Verify Java Installation

```bash
java -version
# Expected: openjdk version "11.0.x" or higher

javac -version
# Expected: javac 11.x or higher
```

#### 3️⃣ Verify Maven Installation

```bash
mvn -version
# Expected: Apache Maven 3.6.0 or higher
```

#### 4️⃣ Install Project Dependencies

```bash
mvn clean install
```

**Expected Output:**
```
[INFO] Scanning for projects...
[INFO] Building OpenLibrary API Tests 1.0.0
...
[INFO] BUILD SUCCESS
[INFO] Total time: 45.123 s
```

---

## 📝 Test Coverage

### Cucumber Feature Tests (author_api.feature)

| # | Scenario | Status |
|---|----------|--------|
| 1️⃣ | Verify Author Endpoint Returns Expected Author Details | ✅ |
| 2️⃣ | Validate Author Response Against JSON Schema | ✅ |
| 3️⃣ | Verify Author Endpoint Response Contains Expected Fields | ✅ |
| 4️⃣ | Verify Author Endpoint Consistency (Sequential Requests) | ✅ |

### JUnit Unit Tests

| # | Class | Tests | Purpose |
|---|-------|-------|---------|
| 1️⃣ | AuthorApiTest | 3 | Direct API validation |
| 2️⃣ | AuthorApiSchemaTest | 3 | JSON Schema validation |
| **Total** | - | **6** | API Contract verification |

### Test Data Source

All assertions get values from feature file:

```gherkin
Scenario: Verify Author Endpoint Returns Expected Author Details
  Then the response status code should be 200                    ← 200
  And the response should contain personal_name as "Sachi Rautroy"     ← "Sachi Rautroy"
  And the response should contain alternate_names with "Yugashrashta Sachi Routray"  ← "Yugashrashta Sachi Routray"
  And the response content type should be "application/json"    ← "application/json"
```

---

## 📊 Report Generation

### Allure Reporting

#### Generate Report Locally

```bash
# Run tests and generate results
mvn clean test

# Generate Allure report
mvn allure:report

# Serve report in browser
mvn allure:serve
```

#### Report Contents

- 📈 **Test Overview**: Pass/fail rates, duration
- 🔗 **Test Hierarchy**: By feature, story, and test
- 📝 **Step Details**: Each Cucumber step with time
- 🔴 **Failures**: With error messages and screenshots
- 📊 **Metrics**: Execution time, test categories
- 📅 **History**: Trend data across runs

#### Report Location

```
target/
├── allure-results/        (JSON test data)
├── site/
│   └── allure-maven-plugin/
│       └── index.html     (Viewable report)
```

### GitHub Actions Artifacts

After workflow run:

1. Go to **Actions Tab**
2. Select latest **Run API Tests** workflow
3. Download artifacts:
   - `test-results` - Surefire XML reports
   - `allure-results` - Allure JSON data  
   - `allure-report` - HTML report

---

## 🐛 Troubleshooting

### Common Issues & Solutions

#### ❌ Issue: Tests fail - "Connection refused"

**Cause**: OpenLibrary API is unreachable

**Solution**:
```bash
# Check internet connectivity
ping openlibrary.org

# Verify API is accessible
curl https://openlibrary.org/authors/OL1A.json
```

#### ❌ Issue: Maven build fails - "Unknown property"

**Cause**: Maven cache corruption

**Solution**:
```bash
# Clear Maven cache
mvn clean -U

# Rebuild
mvn clean install
```

#### ❌ Issue: Tests fail - "Schema validation failed"

**Cause**: Schema file not found or API response changed

**Solution**:
```bash
# Verify schema file exists
ls src/test/resources/schemas/author-schema.json

# Re-run with debug output
mvn test -X
```

#### ❌ Issue: Parallel tests interfere with each other

**Cause**: Context not properly isolated

**Solution**:
```bash
# Run tests sequentially instead
mvn test -DthreadCount=1

# Check ThreadLocal implementation in ScenarioContextHolder
```

#### ❌ Issue: Allure report generation fails

**Cause**: No test results available

**Solution**:
```bash
# Delete old results
rm -rf target/allure-results/

# Run tests again
mvn clean test

# Generate fresh report
mvn allure:report
```

#### ❌ Issue: GitHub Actions workflow not found

**Cause**: Workflow file not in correct location

**Solution**:
```bash
# Verify file location
ls .github/workflows/run-tests.yml

# File must be committed to repository
git add .github/workflows/run-tests.yml
git commit -m "Add GitHub Actions workflow"
git push
```

---

## 📚 Test Implementation Examples

### Cucumber Step Definition Example

```java
@Then("the response should contain personal_name as {string}")
@Step("Verify personal_name is {0}")
public void verifyPersonalName(String expectedName) {
    // ✅ Step 1: Store expected value in context (from feature file)
    context.setExpectedPersonalName(expectedName);
    
    // ✅ Step 2: Get response from context
    Response response = context.getLastResponse();
    
    // ✅ Step 3: Extract actual value
    String actualName = response.jsonPath().getString("personal_name");
    
    // ✅ Step 4: Assert and report
    assertThat(actualName)
        .as("Personal name should be '" + expectedName + "'")
        .isEqualTo(expectedName);
    
    System.out.println("Personal name verified: " + expectedName);
}
```

### JUnit Test Example with Context

```java
@Test
@AllureId("TEST_001")
public void testGetAuthorEndpoint() {
    // ✅ Get expected values from ScenarioContext
    int expectedStatusCode = getExpectedStatusCode();           // 200
    String expectedPersonalName = getExpectedPersonalName();   // "Sachi Rautroy"
    String expectedAlternateName = getExpectedAlternateName(); // "Yugashrashta Sachi Routray"
    
    System.out.println("Test Data from Feature File:");
    System.out.println("  Status: " + expectedStatusCode);
    System.out.println("  Name: " + expectedPersonalName);
    System.out.println("  Alternate: " + expectedAlternateName);
    
    // ✅ Execute API request
    Response response = given()
        .header("Accept", "application/json")
        .when()
        .get(AUTHOR_ENDPOINT);
    
    // ✅ Assert against expected values from context
    response.then()
        .statusCode(expectedStatusCode)
        .body("personal_name", equalTo(expectedPersonalName))
        .body("alternate_names", hasItem(expectedAlternateName));
}
```

---

## 🔄 Continuous Integration

### GitHub Actions Workflow

The project includes `.github/workflows/run-tests.yml`:

```yaml
name: Run API Tests
on:
  workflow_dispatch:     # Manual trigger

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          cache: maven
      - run: mvn clean test
      - run: mvn allure:report
      - uses: actions/upload-artifact@v4
        with:
          name: allure-report
          path: target/site/allure-maven-plugin/
```

---

## 📞 Support & Documentation

### Reference Files

- 📖 **Feature File**: `src/test/resources/features/author_api.feature`
- 🧪 **JUnit Tests**: `src/test/java/com/openlib/tests/`
- 🔗 **Step Definitions**: `src/test/java/com/openlib/stepdefinitions/`
- 📋 **POM Configuration**: `pom.xml`
- ⚙️ **Workflow**: `.github/workflows/run-tests.yml`

### Key Classes

- **ScenarioContext**: Data container for test values
- **ScenarioContextHolder**: Thread-safe context accessor
- **AuthorApiStepDefinitions**: Cucumber step implementations
- **AuthorApiTest**: JUnit unit tests
- **AuthorApiSchemaTest**: Schema validation tests

---

## 📄 License & Version

- **Version**: 1.0.0
- **Created**: February 2026
- **Status**: ✅ Production Ready
- **Framework**: Cucumber + JUnit 5 + REST-Assured + Allure

---

## ✨ Key Features Summary

| Feature | Implementation |
|---------|-----------------|
| 🔗 **Single Source of Truth** | Feature files define all test data |
| 🧪 **BDD + Unit Integration** | Cucumber scenarios share context with JUnit tests |
| 🔄 **Parallel Execution** | ThreadLocal context isolation for safe parallel runs |
| 📊 **Comprehensive Reporting** | Allure reports with step details and artifacts |
| 🚀 **CI/CD Ready** | GitHub Actions workflow with one-click execution |
| 📝 **Maintainable** | Clear separation of concerns and data flow |
| ✅ **No Hardcoding** | All assertions from feature files |
| 🧵 **Thread-Safe** | ScenarioContextHolder for concurrent test execution |

---

**Happy Testing! 🎉**

For issues or contributions, please refer to the code comments and implementation details in the test classes.


4 scenario-based tests with GIVEN-WHEN-THEN structure:
- Verify Author Endpoint Returns Expected Author Details
- Validate Author Response Against JSON Schema
- Verify Author Endpoint Response Contains Expected Fields
- Verify Author Endpoint Consistency

## API Under Test

**Endpoint**: `GET https://openlibrary.org/authors/OL1A.json`

**Expected Response**:
```json
{
  "personal_name": "Sachi Routroy",
  "alternate_names": ["Yugashrashta Sachi Routray"],
  "key": "/authors/OL1A",
  ...
}
```

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Internet connection (to access OpenLibrary API)
- Allure Command Line tool (optional, for viewing reports)

## Installation & Setup

1. **Clone/Navigate to the project directory**:
   ```bash
   cd nsw-revenue-api
   ```

2. **Install dependencies**:
   ```bash
   mvn clean install
   ```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Only Unit Tests
```bash
mvn test -Dtest=AuthorApiTest,AuthorApiSchemaTest
```

### Run Only Cucumber Tests
```bash
mvn test -Dtest=CucumberTestRunner
```

### Run Specific Test Class
```bash
mvn test -Dtest=AuthorApiTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=AuthorApiTest#testGetAuthorEndpoint
```

## Generating & Viewing Reports

### Generate Allure Report
After running tests, generate the Allure report:
```bash
mvn allure:report
```

### View Allure Report
```bash
mvn allure:serve
```

This will start a local web server and open the Allure report in your default browser.

The Allure report will be located in:
- Results: `target/allure-results/`
- Report: `target/allure-report/`

## Test Assertions

### AuthorApiTest.java

**Personal Name Assertion**:
```java
response.then().body("personal_name", equalTo("Sachi Routroy"));
```

**Alternate Names Assertion**:
```java
response.then().body("alternate_names", hasItem("Yugashrashta Sachi Routray"));
```

**Status Code Assertion**:
```java
response.then().statusCode(200);
```

### AuthorApiSchemaTest.java

**Schema Validation**:
```java
response.then().body(matchesJsonSchema(schema));
```

## Gherkin Scenarios (BDD)

All scenarios follow the GIVEN-WHEN-THEN format:

### Scenario 1: Verify Author Endpoint Returns Expected Author Details
```gherkin
GIVEN the OpenLibrary API is available
WHEN a GET request is made to fetch author "OL1A"
THEN the response status code should be 200
AND the response should contain personal_name as "Sachi Routroy"
AND the response should contain alternate_names with "Yugashrashta Sachi Routray"
AND the response content type should be "application/json"
```

### Scenario 2: Validate Author Response Against JSON Schema
```gherkin
GIVEN the OpenLibrary API is available with a defined schema
WHEN a GET request is made to fetch author "OL1A"
THEN the response status code should be 200
AND the response should validate against the author schema
AND the response should contain required fields "key" and "personal_name"
```

## JSON Schema

The schema file `src/test/resources/schemas/author-schema.json` defines the expected structure of the API response:
- Required fields: `key`, `personal_name`
- Expected properties: `name`, `alternate_names`, `birth_date`, `death_date`, `bio`, `website`, `work_count`, `top_work`, `top_subjects`, `type`

## Step Definitions

Step definitions are implemented in `AuthorApiStepDefinitions.java` and support:
- API setup and configuration
- HTTP request execution
- Response validation
- Assertion checking
- Allure reporting with @Step annotations

## Allure Features

The project includes Allure annotations for enhanced reporting:
- `@Feature`: Groups tests by feature (OpenLibrary API, OpenLibrary API Schema Validation)
- `@Story`: Organizes tests by story
- `@AllureId`: Unique test identifiers (TEST_001 through TEST_006)
- `@DisplayName`: Human-readable test names
- `@Description`: Detailed test descriptions with GIVEN-WHEN-THEN format
- `@Step`: Step-level reporting in Allure for Cucumber tests

## Logging & Debugging

Tests have logging enabled:
```java
RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
```

Responses and request details are printed to console for debugging.

## Troubleshooting

### Issue: Tests fail with "Connection refused"
**Solution**: Verify internet connection and OpenLibrary API is accessible at https://openlibrary.org

### Issue: Schema validation fails
**Solution**: Verify schema file exists at `src/test/resources/schemas/author-schema.json`

### Issue: Allure report is empty
**Solution**: 
1. Delete `target/allure-results` folder
2. Run tests again: `mvn clean test`
3. Generate report: `mvn allure:report`

## CI/CD Integration

To integrate with CI/CD pipelines:
```bash
mvn clean test allure:report
```

Generate XML/JSON reports for integration:
```bash
mvn test
```

Reports will be available in `target/allure-results/` for further processing.

## Contact & Support

For issues or questions, please refer to the test implementation files for detailed code comments and assertions.

---

**Project Created**: February 2026
**Version**: 1.0.0
