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
    private final CAClient client;
    
    public HighConcurrencyTestCase(CAClient client) throws DeploymentException, IOException, InterruptedException, Exception {
        
        this.client = client;
        
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void doTest() throws URISyntaxException, DeploymentException, IOException, InterruptedException, Exception {
        AtomicLong count = new AtomicLong();
        int timeoutSeconds = 2;
        int monitorSeconds = 20;
        String[] channelNames = {"counter0", "counter1", "counter2", "counter3", "counter4"};
        Consumer<? super Object> cnsmr = (value) -> count.incrementAndGet();
        List<CAClient> clientList = new ArrayList<>();
        int numClients = 100;
        
        // Create web socket connections
        for (int i = 0; i < numClients; i++) {
            CAClient c = client.getClass().newInstance();
            
            executor.execute(new TestCaseRunner(c, timeoutSeconds, monitorSeconds, cnsmr, channelNames));
            
            clientList.add(client);
        }
        
        executor.shutdown();
        
        executor.awaitTermination(1, TimeUnit.MINUTES); // Wait at least as long as monitorSeconds...
        
        System.out.println("done with test: total updates: " + String.format("%,d", count.get()));
        
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
