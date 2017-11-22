package org.jlab.caclient.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.jlab.caclient.ChannelGroup;

public class WSChannelGroup implements ChannelGroup {
    
    private final Map<String, WSChannel> internalMap = new HashMap<>();
    
    public static WSChannelGroup create(WSContext context, String... channelNames) {
        
        WSChannelGroup cm = new WSChannelGroup();
        
        for (int i = 0; i < channelNames.length; i++) {
            WSChannel c = context.create(channelNames[i]);
            cm.internalMap.put(channelNames[i], c);
        }
        
        return cm;
    }
    
    @Override
    public CompletableFuture<?> connectAsync() {
        List<CompletableFuture<?>> futureList = new ArrayList<>();
        
        for (WSChannel c : internalMap.values()) {
            CompletableFuture<?> future = c.connectAsync();
            futureList.add(future);
        }        
        
        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture<?>[0]));
    }
    
    @Override
    public WSMonitorGroup addValueMonitor(Consumer<? super Object> cnsmr) {
        Map<String, WSMonitor> monitorMap = new HashMap<>();
        
        for (WSChannel c : internalMap.values()) {
            WSMonitor monitor = c.addValueMonitor(cnsmr);
            monitorMap.put(c.getName(), monitor);
        }        
        
        return new WSMonitorGroup(monitorMap);        
    }
    
    @Override
    public void close() {
        RuntimeException closeException = new RuntimeException("Unable to close channel");
        for (WSChannel c : internalMap.values()) {
            try {
                c.close();
            } catch (Exception e) {
                closeException.addSuppressed(e);
            }
        }
        
        if (closeException.getSuppressed().length > 0) {
            throw closeException;
        }
    }
}
