package org.jlab.jts.integration;

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
import org.jlab.caclient.CAClient;
import org.jlab.caclient.ws.WSClient;

/**
 *
 * @author slominskir
 */
public class DeadLockTest {

    private ExecutorService executor;

    public static void main(String[] args) throws Exception {
        new DeadLockTest();
    }
    
    public DeadLockTest() throws DeploymentException, IOException, InterruptedException, Exception {
        executor = Executors.newCachedThreadPool();

        tonsOfConcurrencyTest();
        
        executor.shutdown();
    }

    public void tonsOfConcurrencyTest() throws URISyntaxException, DeploymentException, IOException, InterruptedException, Exception {
        AtomicLong count = new AtomicLong();
        int timeoutSeconds = 2;
        int monitorSeconds = 20;
        String[] channelNames = {"counter0", "counter1", "counter2", "counter3", "counter4"};
        Consumer<? super Object> cnsmr = (value) -> count.incrementAndGet();
        List<CAClient> clientList = new ArrayList<>();
        int numClients = 100;
        
        // Create web socket connections
        for (int i = 0; i < numClients; i++) {
            CAClient client = new WSClient();
            
            executor.execute(new ClientTestRunner(client, timeoutSeconds, monitorSeconds, cnsmr, channelNames));
            
            clientList.add(client);
        }
        
        executor.awaitTermination(1, TimeUnit.HOURS); // Wait for an hour for all stuff to finish
        
        System.out.println("done with test: total updates: " + String.format("%,d", count.get()));
        
        // Close websockets
        for (int i = 0; i < numClients; i++) {
            CAClient client = clientList.get(i);
            client.close();
        }
    }
}
