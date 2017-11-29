package org.jlab.jts.caclient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 *
 * @author slominskir
 */
public interface ChannelGroup extends AutoCloseable {

    public MonitorGroup addValueMonitor(Consumer<? super Object> csmr);    

    public CompletableFuture<?> connectAsync();
}
