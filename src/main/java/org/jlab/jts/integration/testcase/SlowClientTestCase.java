package org.jlab.jts.integration.testcase;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ContainerProvider;
import org.jlab.jts.caclient.CAClient;
import org.jlab.jts.integration.TestCase;
import org.jlab.jts.integration.TestCaseRunner;

/**
 *
 * @author ryans
 */
public class SlowClientTestCase implements TestCase {

    private final Class<? extends CAClient> clazz;
    private final String[] channelNames;

    public SlowClientTestCase(Class<? extends CAClient> clazz) {
        this.clazz = clazz;

        final int numCounters = 5000;

        channelNames = new String[numCounters];
        
        for (int i = 0; i < channelNames.length; i++) {
            channelNames[i] = "counter" + i;
        }
    }

    @Override
    public void doTest() throws InterruptedException, Exception {
        int timeoutSeconds = 5;
        int monitorSeconds = 220;
        AtomicLong updates = new AtomicLong();
        Consumer<? super Object> cnsmr = (value -> {
            updates.incrementAndGet();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SlowClientTestCase.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        // For WebSocket client set the application level recv Q size very small so we quickly put back pressure on epics2web; else Tyrus will buffer 4MB or more
        ContainerProvider.getWebSocketContainer().setDefaultMaxTextMessageBufferSize(512);        
        
        new TestCaseRunner(clazz, timeoutSeconds, monitorSeconds, cnsmr, channelNames).run();
        
        System.out.println("done with test: total updates: " + String.format("%,d", updates.get()));
        System.out.println("average updates per second: " + String.format("%,.2f", updates.get() / Double.valueOf(monitorSeconds)));
    }

    @Override
    public void close() throws Exception {

    }
}
