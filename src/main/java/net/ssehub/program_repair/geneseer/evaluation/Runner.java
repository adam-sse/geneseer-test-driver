package net.ssehub.program_repair.geneseer.evaluation;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

public class Runner {
    
    private static List<TestResult> runClass(String className) {
        TestResultCollector testResultCollector = new TestResultCollector();
        
        try {
            Class<?> testClass = Class.forName(className);
            
            JUnitCore junit = new JUnitCore();
            junit.addListener(testResultCollector);
            
            junit.run(testClass);
            
            
        } catch (ClassNotFoundException e) {
            // ignore
        }
        
        return testResultCollector.getTestResults();
    }
    
    private static TestResult runMethod(String className, String methodName) {
        TestResultCollector testResultCollector = new TestResultCollector();
        
        try {
            Class<?> testclass = Class.forName(className);
            Request request = Request.method(testclass, methodName);
            
            JUnitCore junit = new JUnitCore();
            junit.addListener(testResultCollector);
            junit.run(request);
            
        } catch (ClassNotFoundException e) {
            // ignore
        }
        
        if (testResultCollector.getTestResults().size() != 1) {
            return null;
        } else {
            return testResultCollector.getTestResults().get(0);
        }
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(System.out);
        ObjectInputStream in = new ObjectInputStream(System.in);
        
        System.setOut(new PrintStream(new DiscardingOutputStream()));
        System.setErr(new PrintStream(new DiscardingOutputStream()));
        System.setIn(new EmptyInputStream());
        
        while (true) {
            String command = (String) in.readObject();
            
            switch (command) {
            case "CLASS":
                out.writeObject(runClass((String) in.readObject()));
                out.flush();
                break;
                
            case "METHOD":
                out.writeObject(runMethod((String) in.readObject(), (String) in.readObject()));
                out.flush();
                break;
            }
        }
    }
    
    private static final class DiscardingOutputStream extends OutputStream {
        
        @Override
        public void write(int b) throws IOException {
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }
        
    }
    
    private static final class EmptyInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            return -1;
        }
        
        @Override
        public int read(byte[] b) throws IOException {
            return -1;
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return -1;
        }
        
        @Override
        public byte[] readAllBytes() throws IOException {
            return new byte[0];
        }
        
        @Override
        public int readNBytes(byte[] b, int off, int len) throws IOException {
            return 0;
        }
        
        @Override
        public byte[] readNBytes(int len) throws IOException {
            return new byte[0];
        }
        
    }
    
}
