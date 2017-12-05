package org.jlab.jts.integration;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.jts.caclient.CAClient;
import org.jlab.jts.caclient.ChannelGroup;
import org.jlab.jts.caclient.MonitorGroup;

/**
 *
 * @author slominskir
 */
public class TestCaseRunner implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(TestCaseRunner.class.getName());    
    
    private final CAClient client;
    private final int timeoutSeconds;
    private final int monitorSeconds;
    private final Consumer<? super Object> cnsmr;
    private final String[] channelNames;

    public TestCaseRunner(CAClient client, int timeoutSeconds, int monitorSeconds, Consumer<? super Object> cnsmr, String... channelNames) {
        this.client = client;
        this.timeoutSeconds = timeoutSeconds;
        this.monitorSeconds = monitorSeconds;
        this.cnsmr = cnsmr;
        this.channelNames = channelNames;
    }

    @Override
    public void run() {
        try (ChannelGroup channels = client.create(channelNames)) {
            channels.connectAsync().get(timeoutSeconds, TimeUnit.SECONDS);

            try (MonitorGroup monitors = channels.addValueMonitor(cnsmr)) {
                Thread.sleep(monitorSeconds * 1000);
            }
            
            // Wait a few seconds for monitor close back and forth to avoid client and server abrupt close warnings (just being polite)
            LOGGER.log(Level.INFO, "Waiting for orderly shutdown of monitors before closing socket...");
            Thread.sleep(10000); // It takes a very long time to close monitors over a websocket in an orderly fashion; whereas CAJ can close 5000 PVs updating at 100Hz in less than 2 seconds
        } catch (Exception ex) {
            Logger.getLogger(TestCaseRunner.class.getName()).log(Level.SEVERE, "Test Case Exception:", ex);
        }
        
        //System.out.println("done with test runner");
    }
}
