package com.openlib.tests;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.*;

/**
 * Cucumber Test Runner for OpenLibrary API Tests
 * This runner executes all feature files with proper configuration for Allure reporting
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "progress, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.openlib.stepdefinitions")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")
@DisplayName("OpenLibrary API Cucumber Tests")
public class CucumberTestRunner {
}
