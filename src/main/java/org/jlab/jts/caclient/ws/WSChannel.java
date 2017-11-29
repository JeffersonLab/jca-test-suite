package org.jlab.jts.caclient.ws;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 *
 * @author slominskir
 */
public class WSChannel implements AutoCloseable {

    private final WSContext context;
    private final String name;
    private final CopyOnWriteArrayList<WSMonitor> monitorList = new CopyOnWriteArrayList<>();
    CompletableFuture<?> connectFuture = new CompletableFuture<>();
    
    protected WSChannel(WSContext context, String name) {
        this.context = context;
        this.name = name;
    }
    
    @Override
    public void close() throws Exception {
        context.close(this);
    }

    public CompletableFuture<?> connectAsync() {
        return context.connectAsync(this);
    }

    public String getName() {
        return name;
    }

    public WSMonitor addValueMonitor(Consumer<? super Object> cnsmr) {
        WSMonitor monitor = new WSMonitor(cnsmr);
        
        monitorList.add(monitor);
        
        return monitor;
    }

    void updateValueMonitors(Object value) {
        monitorList.forEach((monitor) -> {
            monitor.update(value);
        });
    }
}
