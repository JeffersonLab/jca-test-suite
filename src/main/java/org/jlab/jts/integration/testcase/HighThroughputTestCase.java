package org.jlab.jts.integration.testcase;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.jlab.jts.caclient.CAClient;
import org.jlab.jts.integration.TestCase;
import org.jlab.jts.integration.TestCaseRunner;

/**
 *
 * @author ryans
 */
public class HighThroughputTestCase implements TestCase {

    private final Class<? extends CAClient> clazz;
    private final String[] channelNames;

    public HighThroughputTestCase(Class<? extends CAClient> clazz) {
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
        int monitorSeconds = 30;
        AtomicLong updates = new AtomicLong();
        Consumer<? super Object> cnsmr = (value -> updates.incrementAndGet());

        new TestCaseRunner(clazz, timeoutSeconds, monitorSeconds, cnsmr, channelNames).run();
        
        System.out.println("done with test: total updates: " + String.format("%,d", updates.get()));
        System.out.println("average updates per second: " + String.format("%,.2f", updates.get() / Double.valueOf(monitorSeconds)));
    }

    @Override
    public void close() throws Exception {

    }
}
