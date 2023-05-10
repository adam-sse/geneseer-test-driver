package net.ssehub.program_repair.geneseer.evaluation;

import java.io.Serializable;

public class TestResult implements Serializable {

    private static final long serialVersionUID = 5281136086896771809L;

    private String testClass;
    
    private String testMethod;
    
    private String failureMessage;
    
    private String failureStacktrace;
    

    public TestResult(String testClass, String testMethod) {
        this.testClass = testClass;
        this.testMethod = testMethod;
    }
    
    public TestResult(String testClass, String testMethod, String failureMessage, String failureStacktrace) {
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.failureMessage = failureMessage;
        this.failureStacktrace = failureStacktrace;
    }
    
    public boolean isFailure() {
        return failureStacktrace != null;
    }
    
    public String getTestClass() {
        return testClass;
    }
    
    public String getTestMethod() {
        return testMethod;
    }
    
    public String getFailureMessage() {
        return failureMessage;
    }
    
    void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
    
    public String getFailureStacktrace() {
        return failureStacktrace;
    }
    
    void setFailureStacktrace(String failureStacktrace) {
        this.failureStacktrace = failureStacktrace;
    }
    
    @Override
    public String toString() {
        return testClass + "::" + testMethod;
    }
    
}
