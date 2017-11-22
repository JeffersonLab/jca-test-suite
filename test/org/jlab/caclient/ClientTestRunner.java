package org.jlab.caclient;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author slominskir
 */
public class ClientTestRunner implements Runnable {

    private final CAClient client;
    private final int timeoutSeconds;
    private final int monitorSeconds;
    private final Consumer<? super Object> cnsmr;
    private final String[] channelNames;

    public ClientTestRunner(CAClient client, int timeoutSeconds, int monitorSeconds, Consumer<? super Object> cnsmr, String... channelNames) {
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
        } catch (Exception ex) {
            Logger.getLogger(ClientTestRunner.class.getName()).log(Level.SEVERE, "ClientTestRunner Exception", ex);
        }
    }
}
