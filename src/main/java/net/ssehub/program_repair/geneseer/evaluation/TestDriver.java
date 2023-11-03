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
import org.junit.runner.Request;

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

        debugMsg("Running all tests in class" + className);
        Class<?> testClass = Class.forName(className);
        
        JUnitCore junit = new JUnitCore();
        junit.addListener(testResultCollector);
        junit.run(testClass);
        
        debugMsg("Got " + testResultCollector.getTestResults().size() + " TestResults");
        return testResultCollector.getTestResults();
    }
    
    private static TestResult runMethod(String className, String methodName) throws ClassNotFoundException {
        TestResultCollector testResultCollector = new TestResultCollector();

        debugMsg("Running method " + methodName + " in class" + className);
        Class<?> testclass = Class.forName(className);
        Request request = Request.method(testclass, methodName);
        
        JUnitCore junit = new JUnitCore();
        junit.addListener(testResultCollector);
        junit.run(request);
        
        TestResult result;
        if (testResultCollector.getTestResults().size() != 1) {
            debugMsg("Got no TestResult");
            result = null;
        } else {
            debugMsg("Got " + testResultCollector.getTestResults().size() + " TestResults");
            result = testResultCollector.getTestResults().get(0);
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(System.out);
        ObjectInputStream in = new ObjectInputStream(System.in);
        stderr = System.err;
        
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
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
                    
                case "METHOD":
                    out.writeObject(runMethod((String) in.readObject(), (String) in.readObject()));
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
        }
    }
    
    private static final class UncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread thread, Throwable exception) {
            if (!(exception instanceof ThreadDeath)) {
                stderr.print("[geneseer-test-driver] Exception in thread \"" + thread.getName() + "\" ");
                exception.printStackTrace(stderr);
                stderr.flush();
                
                if (!debug) {
                    System.err.print("Exception in thread \"" + thread.getName() + "\" ");
                    exception.printStackTrace(System.err);
                }
            }
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
        
        @Override
        public byte[] readAllBytes() throws IOException {
            return new byte[0];
        }
        
        @Override
        public int readNBytes(byte[] bytes, int off, int len) throws IOException {
            return 0;
        }
        
        @Override
        public byte[] readNBytes(int len) throws IOException {
            return new byte[0];
        }
        
    }
    
}
