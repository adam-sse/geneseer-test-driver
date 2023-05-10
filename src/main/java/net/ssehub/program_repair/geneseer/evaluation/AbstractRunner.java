package net.ssehub.program_repair.geneseer.evaluation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.List;

public abstract class AbstractRunner {

    private static boolean noWrap = System.getProperty("nowrap") != null;
    
    protected abstract List<TestResult> runTests(String[] args);
    
    public final void run(String[] args) throws IOException {
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
            result = runTests(args);
            
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
