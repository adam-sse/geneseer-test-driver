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
    
    private ObjectInputStream in;
    
    private ObjectOutputStream out;
    
    private boolean usePerTestClassLoader;
    
    public TestDriver(InputStream stdin, OutputStream stdout, boolean usePerTestClassLoader) throws IOException {
        this.usePerTestClassLoader = usePerTestClassLoader;
        this.out = new ObjectOutputStream(stdout);
        this.in = new ObjectInputStream(stdin);
    }
    
    private List<TestResult> runClass(String className) throws ClassNotFoundException, IOException {
        debugMsg("Running test class " + className);
        return runTestClass(className, null);
    }
    
    private List<TestResult> runMethodsReportingIndividually(String className)
            throws ClassNotFoundException, IOException {
        debugMsg("Running methods in test class " + className + ", reporting finished tests individually");
        TestFinishReporter testFinishReporter = new TestFinishReporter(in, out);
        return runTestClass(className, testFinishReporter);
    }
    
    private List<TestResult> runTestClass(String className, TestFinishReporter testFinishReporter)
            throws ClassNotFoundException, IOException {
        TestResultCollector testResultCollector = new TestResultCollector();
        
        TestClassLoader loader = null;
        try {
            Class<?> testClass;
            if (usePerTestClassLoader) {
                loader = new TestClassLoader();
                testClass = Class.forName(className, true, loader);
            } else {
                testClass = Class.forName(className);
            }
            
            JUnitCore junit = new JUnitCore();
            junit.addListener(testResultCollector);
            if (testFinishReporter != null) {
                junit.addListener(testFinishReporter);
            }
            junit.run(testClass);
        } finally {
            if (loader != null) {
                loader.close();
            }
        }
        
        debugMsg("Got " + testResultCollector.getTestResults().size() + " TestResults");
        return testResultCollector.getTestResults();
    }
    
    private void run() throws ClassNotFoundException, IOException {
        if (debug) {
            System.setOut(stderr);
            System.setErr(stderr);
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
            debugMsg("Debug output enabled");
        } else {
            System.setOut(new PrintStream(new DiscardingOutputStream()));
            System.setErr(new PrintStream(new DiscardingOutputStream()));
        }
        System.setIn(new EmptyInputStream());
        
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
                    List<TestResult> resultList = runMethodsReportingIndividually((String) in.readObject());
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
                    debugMsg("Unknown command: " + command);
                    System.exit(1);
                    break;
                }
            }
        } catch (ClassCastException e) {
            throw new IOException("Protocol error", e);
        } catch (EOFException e) {
            debugMsg("stdin closed, stopping...");
        }
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        stderr = System.err;
        
        boolean usePerTestClassLoader = true;
        for (String arg : args) {
            if ("--debug".equalsIgnoreCase(arg)) {
                debug = true;
            } else if ("--no-per-test-classloader".equalsIgnoreCase(arg)) {
                usePerTestClassLoader = false;
            } else {
                stderr.println("Warning: unknown command line option: " + arg);
            }
        }
        
        TestDriver driver = new TestDriver(System.in, System.out, usePerTestClassLoader);
        driver.run();
        
        // explicitly shut down JVM, since tests may have lingering threads
        System.exit(0);
    }
    
    static void debugMsg(String message) {
        if (debug) {
            stderr.println("[geneseer-test-driver] " + message);
            stderr.flush();
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
