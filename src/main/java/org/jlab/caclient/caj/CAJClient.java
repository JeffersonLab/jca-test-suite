package org.jlab.caclient.caj;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.DefaultConfiguration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.jlab.caclient.CAClient;
import org.jlab.caclient.ChannelGroup;

/**
 *
 * @author slominskir
 */
public class CAJClient implements CAClient {

    private final CAJContext context;
    
    public CAJClient() throws CAException, InterruptedException, ExecutionException, TimeoutException {
        JCALibrary jca = JCALibrary.getInstance();

        DefaultConfiguration config = new DefaultConfiguration("config");

        config.setAttribute("class", JCALibrary.CHANNEL_ACCESS_JAVA);
        //config.setAttribute("addr_list", "localhost");
        //config.setAttribute("auto_addr_list", "false");

        context = (CAJContext) jca.createContext(config);

        context.initialize();
    }

    private void doTest() throws InterruptedException, CAException, ExecutionException, TimeoutException {
        AtomicLong updates = new AtomicLong();

        final int numCounters = 100;
        final int secondsToSleep = 20;
        String[] channelNames = new String[numCounters];

        for (int i = 0; i < channelNames.length; i++) {
            channelNames[i] = "counter" + i;
        }

        long start = 0;
        try (CAJChannelGroup channels = CAJChannelGroup.create(context, channelNames)) {
            System.out.println("Connecting to channels");
            channels.connectAsync().get(2, TimeUnit.SECONDS);

            Consumer<? super Object> c = (value -> updates.incrementAndGet());
            //Consumer<? super Object> c = (value -> System.out.println(value));

            start = System.currentTimeMillis();
            try (CAJMonitorGroup monitors = channels.addValueMonitor(c)) {
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

    public static void main(String[] args) {
        try (CAClient client = new CAJClient()){
        } catch (Exception e) {
            System.err.println("Caught an exception");
            e.printStackTrace();
        }
    }

    @Override
    public ChannelGroup create(String[] channelNames) {
        return CAJChannelGroup.create(context, channelNames);
    }

    @Override
    public void close() throws Exception {
        context.dispose();
    }
}
