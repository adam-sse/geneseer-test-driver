package net.ssehub.program_repair.geneseer.evaluation;


import java.io.Serializable;

public class TestFailure implements Serializable {
    
    private static final long serialVersionUID = 349718598568296233L;

    private String testClass;
    
    private String testMethod;
    
    private String message;
    
    private String stacktrace;
    
    public TestFailure(org.junit.runner.notification.Failure junitFailure) {
        this.testClass = junitFailure.getDescription().getClassName();
        this.testMethod = junitFailure.getDescription().getMethodName();
        this.message = junitFailure.getMessage();
        this.stacktrace = junitFailure.getTrimmedTrace();
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getStacktrace() {
        return stacktrace;
    }
    
    @Override
    public String toString() {
        return testClass + "::" + testMethod;
    }

}
