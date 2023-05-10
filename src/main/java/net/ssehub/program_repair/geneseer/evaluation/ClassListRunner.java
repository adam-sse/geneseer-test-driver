package net.ssehub.program_repair.geneseer.evaluation;

import java.io.IOException;
import java.util.List;

import org.junit.runner.JUnitCore;

public class ClassListRunner extends AbstractRunner {

    @Override
    protected List<TestResult> runTests(String[] args) {
        Class<?>[] classes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            try {
                classes[i] = Class.forName(args[i]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        
        JUnitCore junit = new JUnitCore();
        TestResultCollector testResultCollector = new TestResultCollector();
        junit.addListener(testResultCollector);
        junit.run(classes);
        
        return testResultCollector.getTestResults();        
    }
    
    public static void main(String[] args) throws IOException {
        new ClassListRunner().run(args);
    }

}
