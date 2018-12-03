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

    private final Class<? extends CAClient> clazz;
    private final int timeoutSeconds;
    private final int monitorSeconds;
    private final Consumer<? super Object> cnsmr;
    private final String[] channelNames;

    public TestCaseRunner(Class<? extends CAClient> clazz, int timeoutSeconds, int monitorSeconds, Consumer<? super Object> cnsmr, String... channelNames) {
        this.clazz = clazz;
        this.timeoutSeconds = timeoutSeconds;
        this.monitorSeconds = monitorSeconds;
        this.cnsmr = cnsmr;
        this.channelNames = channelNames;
    }

    @Override
    public void run() {

        try (CAClient client = clazz.newInstance()) { // AutoClose
            try (ChannelGroup channels = client.create(channelNames)) { // AutoClose
                
                channels.connectAsync().get(timeoutSeconds, TimeUnit.SECONDS);
                
                try (MonitorGroup monitors = channels.addValueMonitor(cnsmr)) { // AutoClose
                    Thread.sleep(monitorSeconds * 1000);
                }
            } catch (Exception ex) {
                Logger.getLogger(TestCaseRunner.class.getName()).log(Level.SEVERE, "Test Case Exception:", ex);
            }
        } catch (Exception ex) {
            Logger.getLogger(TestCaseRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
