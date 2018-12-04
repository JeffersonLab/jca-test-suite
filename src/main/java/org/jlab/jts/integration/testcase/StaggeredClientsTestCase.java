package org.jlab.jts.integration.testcase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import javax.websocket.DeploymentException;
import org.jlab.jts.caclient.CAClient;
import org.jlab.jts.integration.TestCase;
import org.jlab.jts.integration.TestCaseRunner;

/**
 *
 * @author slominskir
 */
public class StaggeredClientsTestCase implements TestCase {

    private final ExecutorService executor;
    private final String[] channelNames;
    private final Class<? extends CAClient> clazz;
    
    public StaggeredClientsTestCase(Class<? extends CAClient> clazz) throws DeploymentException, IOException, InterruptedException, Exception {
        
        this.clazz = clazz;
        
        executor = Executors.newCachedThreadPool();
        
        final int numCounters = 5;

        channelNames = new String[numCounters];
        
        for (int i = 0; i < channelNames.length; i++) {
            channelNames[i] = "counter" + i;
        }        
    }

    @Override
    public void doTest() throws URISyntaxException, DeploymentException, IOException, InterruptedException, Exception {
        AtomicLong count = new AtomicLong();
        int timeoutSeconds = 10;
        int monitorSeconds = 3;
        Consumer<? super Object> cnsmr = (value) -> count.incrementAndGet();
        int numClients = 1000;
        
        long start = System.currentTimeMillis();
        
        // Create web socket connections
        for (int i = 0; i < numClients; i++) {            
            executor.execute(new TestCaseRunner(clazz, timeoutSeconds, monitorSeconds, cnsmr, channelNames));
            
            Thread.sleep(200); // Create five new clients per second            
        }
                
        executor.shutdown();
        
        boolean finished = executor.awaitTermination(1, TimeUnit.MINUTES); // Wait at least as long as monitorSeconds...
        
        if(!finished) {
            System.out.println("timeout");
        }
        
        long stop = System.currentTimeMillis();
        
        long durationSeconds = ((stop - start) / 1000);
        
        System.out.println("duration (seconds): " + durationSeconds);
        System.out.println("done with test: total updates: " + String.format("%,d", count.get()));
        System.out.println("average updates per second: " + String.format("%,.2f", count.get() / Double.valueOf(durationSeconds)));        
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();        
    }
}
