package net.ssehub.program_repair.geneseer.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class TestResultCollector extends RunListener {

    private Map<Description, TestResult> executedTests;
    
    public TestResultCollector() {
        this.executedTests = new LinkedHashMap<>();
    }
    
    public List<TestResult> getTestResults() {
        List<TestResult> list = new ArrayList<>(executedTests.size());
        for (TestResult result : executedTests.values()) {
            list.add(result);
        }
        return list;
    }

    @Override
    public void testStarted(Description description) {
        this.executedTests.put(description, new TestResult(description.getClassName(), description.getMethodName()));
    }
    
    @Override
    public void testFailure(Failure failure) {
        TestResult testResult = this.executedTests.get(failure.getDescription());
        if (testResult != null) {
            testResult.setFailureMessage(failure.getMessage());
            testResult.setFailureStacktrace(failure.getTrimmedTrace());
        } else {
            throw new RuntimeException("Test failed that wasn't started");
        }
    }
    
}
