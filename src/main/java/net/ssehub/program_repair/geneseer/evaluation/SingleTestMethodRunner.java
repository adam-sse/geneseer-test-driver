package net.ssehub.program_repair.geneseer.evaluation;

import java.io.IOException;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

public class SingleTestMethodRunner extends AbstractRunner {

    @Override
    protected List<TestResult> runTests(String[] args) {
        TestResultCollector testResultCollector = new TestResultCollector();
        
        try {
            Class<?> testclass = Class.forName(args[0]);
            Request request = Request.method(testclass, args[1]);
            
            JUnitCore junit = new JUnitCore();
            junit.addListener(testResultCollector);
            junit.run(request);
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        return testResultCollector.getTestResults();  
    }
    
    public static void main(String[] args) throws IOException {
        new SingleTestMethodRunner().run(args);
    }

}
