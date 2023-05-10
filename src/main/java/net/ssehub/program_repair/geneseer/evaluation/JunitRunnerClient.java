package net.ssehub.program_repair.geneseer.evaluation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.runner.JUnitCore;

public class JunitRunnerClient {

    private static boolean noWrap = System.getProperty("nowrap") != null;
    
    public static void main(String[] args) throws IOException {
        ObjectOutputStream stdout = null;
        
        ByteArrayOutputStream capturedStdout = new ByteArrayOutputStream();
        ByteArrayOutputStream capturedStderr = new ByteArrayOutputStream();
        
        if (!noWrap) {
            stdout = new ObjectOutputStream(System.out);
            
            System.setOut(new PrintStream(capturedStdout));
            System.setErr(new PrintStream(capturedStderr));
        }
        
        List<TestResult> result = null;
        
        try {
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
            result = testResultCollector.getTestResults();
            
        } finally {
            capturedStdout.flush();
            capturedStderr.flush();
            
            if (!noWrap) {
                stdout.writeObject(capturedStdout.toString());
                stdout.writeObject(capturedStderr.toString());
            }
            
            if (!noWrap) {
                stdout.writeObject(result);
                stdout.flush();
            } else {
                
                System.out.println(result.size() + " tests executed");
                int numFailed = 0;
                StringBuilder failures = new StringBuilder();
                for (TestResult test : result) {
                    if (test.isFailure()) {
                        numFailed++;
                        failures.append(test).append(' ').append(test.getFailureStacktrace()).append('\n');
                    }
                }
                
                System.out.println(numFailed + " test failures");
                System.out.println(failures.toString());
            }
        }
        
        System.exit(0);
    }

}
