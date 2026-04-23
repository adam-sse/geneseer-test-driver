package net.ssehub.program_repair.geneseer.evaluation;

import java.io.Serializable;

public class TestResult implements Serializable {

    private static final long serialVersionUID = -4814081494206714329L;

    private String testClass;
    
    private String implementingClass;
    
    private String testMethod;
    
    private String failureStacktrace;
    
    TestResult(String implementingClass, String testMethod) {
        this.implementingClass = implementingClass;
        this.testMethod = testMethod;
    }
    
    public boolean isFailure() {
        return failureStacktrace != null;
    }
    
    public String getTestClass() {
        return testClass;
    }
    
    void setTestClass(String testClass) {
        this.testClass = testClass;
    }
    
    public String getImplementingClass() {
        return implementingClass;
    }
    
    public String getTestMethod() {
        return testMethod;
    }
    
    public String getFailureStacktrace() {
        return failureStacktrace;
    }
    
    void setFailureStacktrace(String failureStacktrace) {
        this.failureStacktrace = failureStacktrace;
    }
    
    @Override
    public String toString() {
        return testClass + "::" + testMethod + "@" + implementingClass;
    }
    
}
