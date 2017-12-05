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
public class HighConcurrencyTestCase implements TestCase {

    private final ExecutorService executor;
    private final String[] channelNames;
    private final Class<? extends CAClient> clazz;
    
    public HighConcurrencyTestCase(Class<? extends CAClient> clazz) throws DeploymentException, IOException, InterruptedException, Exception {
        
        this.clazz = clazz;
        
        executor = Executors.newCachedThreadPool();
        
        final int numCounters = 100;

        channelNames = new String[numCounters];
        
        for (int i = 0; i < channelNames.length; i++) {
            channelNames[i] = "counter" + i;
        }        
    }

    @Override
    public void doTest() throws URISyntaxException, DeploymentException, IOException, InterruptedException, Exception {
        AtomicLong count = new AtomicLong();
        int timeoutSeconds = 10;
        int monitorSeconds = 45;
        Consumer<? super Object> cnsmr = (value) -> count.incrementAndGet();
        List<CAClient> clientList = new ArrayList<>();
        int numClients = 100;
        
        // Create web socket connections
        for (int i = 0; i < numClients; i++) {
            CAClient c = clazz.newInstance();
            
            executor.execute(new TestCaseRunner(c, timeoutSeconds, monitorSeconds, cnsmr, channelNames));
            
            clientList.add(c);
        }
        
        executor.shutdown();
        
        executor.awaitTermination(1, TimeUnit.MINUTES); // Wait at least as long as monitorSeconds...
        
        System.out.println("done with test: total updates: " + String.format("%,d", count.get()));
        System.out.println("average updates per second: " + String.format("%,.2f", count.get() / Double.valueOf(monitorSeconds)));        
        
        for (int i = 0; i < numClients; i++) {
            CAClient client = clientList.get(i);
            client.close();
        }
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();        
    }
}
