package org.jlab.jts.integration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.jlab.caclient.CAClient;
import org.jlab.caclient.ChannelGroup;
import org.jlab.caclient.MonitorGroup;
import org.jlab.caclient.caj.CAJClient;
import org.jlab.caclient.j8.J8Client;
import org.jlab.caclient.ws.WSClient;

/**
 *
 * @author slominskir
 */
public class CAClientTest {
    
    private final int numCounters = 10;
    private final int secondsToSleep = 10;    
    private final String[] channelNames = new String[numCounters];
    
    public static void main(String[] args) throws Exception {
        new CAClientTest();
    }
    
    public CAClientTest() throws ExecutionException, TimeoutException, Exception {
        for (int i = 0; i < channelNames.length; i++) {
            channelNames[i] = "counter" + i;
        }             
        
        testj5();
        //testj8();
        //testws();
    }
    
    public void testj5() throws InterruptedException, ExecutionException, TimeoutException, Exception {
        try (CAClient client = new CAJClient()) {
            doClientTest(client, channelNames);
        }    
    }    
    
    public void testj8() throws InterruptedException, ExecutionException, TimeoutException, Exception {
        try (CAClient client = new J8Client()) {
            doClientTest(client, channelNames);
        }    
    }

    public void testws() throws InterruptedException, ExecutionException, TimeoutException, Exception {
        try (CAClient client = new WSClient()) {
            doClientTest(client, channelNames);
        }    
    }   
    
    public void doClientTest(CAClient client, String... channelNames) throws InterruptedException, Exception {
        AtomicLong updates = new AtomicLong();

        long start = 0;
        try (ChannelGroup channels = client.create(channelNames)) {
            System.out.println("Connecting to channels");
            channels.connectAsync().get(2, TimeUnit.SECONDS);

            Consumer<? super Object> c = (value -> updates.incrementAndGet());
            //Consumer<? super Object> c = (value -> System.out.println(value.getClass()));

            start = System.currentTimeMillis();
            try (MonitorGroup monitors = channels.addValueMonitor(c)) {
                Thread.sleep(secondsToSleep * 1000);
            }
        }
        long end = System.currentTimeMillis();

        double secondsActuallyRun = ((end - start) / 1000.0f);

        System.out.println("seconds actually run: " + String.format("%,.2f", secondsActuallyRun));
        System.out.println("requested updates: " + String.format("%,d", numCounters * 1000 * secondsToSleep));
        System.out.println("done with test: total updates: " + String.format("%,d", updates.get()));
        System.out.println("average updates per second: " + String.format("%,.2f", updates.get() / secondsActuallyRun));        
    }    
}
