package net.ssehub.program_repair.geneseer.evaluation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

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
        
        Result result = null;
        
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
            result = junit.run(classes);
            
        } finally {
            capturedStdout.flush();
            capturedStderr.flush();
            
            if (!noWrap) {
                stdout.writeObject(capturedStdout.toString());
                stdout.writeObject(capturedStderr.toString());
            }
            
            List<TestFailure> testFailures = new ArrayList<>(result != null ? result.getFailureCount() : 0);
            if (result != null) {
                for (Failure failure : result.getFailures()) {
                    testFailures.add(new TestFailure(failure));
                }
            }
            
            if (!noWrap) {
                stdout.writeObject(testFailures);
                stdout.flush();
            } else {
                System.out.println(testFailures.size() + " test failures");
                for (TestFailure failure : testFailures) {
                    System.out.println(failure + " " + failure.getStacktrace());
                }
            }
        }
    }

}
