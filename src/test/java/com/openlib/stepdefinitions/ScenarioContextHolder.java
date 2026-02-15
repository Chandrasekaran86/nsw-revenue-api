package com.openlib.stepdefinitions;

/**
 * Thread-safe holder for the current scenario context
 * This allows JUnit tests to access test data from the most recent Cucumber scenario
 */
public class ScenarioContextHolder {
    private static final ThreadLocal<ScenarioContext> contextHolder = ThreadLocal.withInitial(ScenarioContext::new);
    
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
