package net.ssehub.program_repair.geneseer.evaluation;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.runner.JUnitCore;

public class TestDriver {

    private static boolean debug;
    
    private static PrintStream stderr;
    
    private static void debugMsg(String message) {
        if (debug) {
            stderr.println("[geneseer-test-driver] " + message);
            stderr.flush();
        }
    }
    
    private static List<TestResult> runClass(String className) throws ClassNotFoundException {
        TestResultCollector testResultCollector = new TestResultCollector();

        debugMsg("Running test class " + className);
        Class<?> testClass = Class.forName(className);
        
        JUnitCore junit = new JUnitCore();
        junit.addListener(testResultCollector);
        junit.run(testClass);
        
        debugMsg("Got " + testResultCollector.getTestResults().size() + " TestResults");
        return testResultCollector.getTestResults();
    }
    
    private static List<TestResult> runMethodsReportingIndividually(String className, ObjectInputStream in,
            ObjectOutputStream out) throws ClassNotFoundException {
        
        TestResultCollector testResultCollector = new TestResultCollector();
        TestFinishReporter testFinishReporter = new TestFinishReporter(in, out);

        debugMsg("Running methods in test class " + className + ", reporting finished tests individually");
        Class<?> testclass = Class.forName(className);
        
        JUnitCore junit = new JUnitCore();
        junit.addListener(testResultCollector);
        junit.addListener(testFinishReporter);
        junit.run(testclass);
        
        debugMsg("Got " + testResultCollector.getTestResults().size() + " TestResults");
        return testResultCollector.getTestResults();
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(System.out);
        ObjectInputStream in = new ObjectInputStream(System.in);
        stderr = System.err;
        
        System.setIn(new EmptyInputStream());
        System.setOut(new PrintStream(new DiscardingOutputStream()));
        System.setErr(new PrintStream(new DiscardingOutputStream()));
        
        debug = args.length > 0 && args[0].equalsIgnoreCase("DEBUG");
        if (debug) {
            debugMsg("Debug output enabled");
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
            System.setOut(stderr);
            System.setErr(stderr);
        }
        
        try {
            while (true) {
                debugMsg("Waiting for command...");
                String command = (String) in.readObject();
                debugMsg("Received command: " + command);
                switch (command) {
                case "CLASS":
                    out.writeObject(runClass((String) in.readObject()));
                    out.flush();
                    break;
                    
                case "METHODS":
                    List<TestResult> resultList = runMethodsReportingIndividually((String) in.readObject(), in, out);
                    out.writeObject("DONE");
                    out.writeObject(resultList);
                    out.flush();
                    break;
                    
                case "HEARTBEAT":
                    debugMsg("Answering heartbeat with \"alive\"");
                    out.writeObject("alive");
                    out.flush();
                    break;
                    
                default:
                    debugMsg("Ignoring unknown command");
                    break;
                }
            }
        } catch (EOFException e) {
            debugMsg("stdin closed, stopping...");
            System.exit(0);
        }
    }
    
    private static final class ShutdownHook extends Thread {
        
        @Override
        public void run() {
            debugMsg("Shutting down");
        }
        
    }
    
    private static final class DiscardingOutputStream extends OutputStream {
        
        @Override
        public void write(int singleByte) throws IOException {
        }
        
        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
        }

        @Override
        public void write(byte[] bytes) throws IOException {
        }
        
    }
    
    private static final class EmptyInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            return -1;
        }
        
        @Override
        public int read(byte[] bytes) throws IOException {
            return -1;
        }
        
        @Override
        public int read(byte[] bytes, int off, int len) throws IOException {
            return -1;
        }
        
    }
    
}
