package org.jlab.jts.integration;

import java.util.function.Consumer;
import org.jlab.caclient.CAClient;

/**
 *
 * @author ryans
 */
public class TestCaseParameters {

    private final CAClient client;
    private final int timeoutSeconds;
    private final int monitorSeconds;
    private final Consumer<? super Object> cnsmr;
    private final String[] channelNames;

    public TestCaseParameters(CAClient client, int timeoutSeconds, int monitorSeconds, Consumer<? super Object> cnsmr, String[] channelNames) {
        this.client = client;
        this.timeoutSeconds = timeoutSeconds;
        this.monitorSeconds = monitorSeconds;
        this.cnsmr = cnsmr;
        this.channelNames = channelNames;
    }

    public CAClient getClient() {
        return client;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getMonitorSeconds() {
        return monitorSeconds;
    }

    public Consumer<? super Object> getCnsmr() {
        return cnsmr;
    }

    public String[] getChannelNames() {
        return channelNames;
    }
}
