package net.ssehub.program_repair.geneseer.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

class TestResultCollector extends RunListener {

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
    
    private static String escapeInvalidUtf16(String s) {
        StringBuilder out = new StringBuilder(s.length());

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (Character.isHighSurrogate(ch)) {
                if (i + 1 < s.length() && Character.isLowSurrogate(s.charAt(i + 1))) {
                    out.append(ch).append(s.charAt(++i));
                } else {
                    out.append("\\u").append(String.format("%04X", (int) ch));
                }
            } else if (Character.isLowSurrogate(ch)) {
                out.append("\\u").append(String.format("%04X", (int) ch));
            } else {
                out.append(ch);
            }
        }

        return out.toString();
    }
    
    @Override
    public void testFailure(Failure failure) {
        TestResult testResult = this.executedTests.get(failure.getDescription());
        if (testResult != null) {
            
            if (failure.getDescription().getClassName().equals("org.junit.runner.manipulation.Filter")
                    && failure.getMessage().contains("No tests found matching Method")) {
                // this "failure" is created when a non-existing test method name is requested; we don't want it
                this.executedTests.remove(failure.getDescription());
                
            } else {
                testResult.setFailureStacktrace(escapeInvalidUtf16(failure.getTrimmedTrace()));
            }
            
        } else {
            throw new IllegalStateException("Test failed that wasn't started");
        }
    }
    
}
