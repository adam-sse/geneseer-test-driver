package net.ssehub.program_repair.geneseer.evaluation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

class TestFinishReporter extends RunListener {

    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    public TestFinishReporter(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;
    }
    
    @Override
    public void testFinished(Description description) throws IOException, ClassNotFoundException {
        out.writeObject("TEST_FINISHED");
        out.writeObject(description.getClassName());
        out.writeObject(description.getMethodName());
        out.flush();
        
        Object reply = in.readObject();
        if (!"CONTINUE".equals(reply)) {
            throw new IOException("Expected reply \"CONTINUE\", but got " + reply);
        }
    }
    
}
