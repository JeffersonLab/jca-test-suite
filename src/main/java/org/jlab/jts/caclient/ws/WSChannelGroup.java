package org.jlab.jts.caclient.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.jlab.jts.caclient.ChannelGroup;

public class WSChannelGroup implements ChannelGroup {
    
    private final WSContext context;
    private final Map<String, WSChannel> internalMap = new HashMap<>();
    
    private WSChannelGroup(WSContext context) {
        this.context = context;
    }    
    
    public static WSChannelGroup create(WSContext context, String... channelNames) {
        
        WSChannelGroup cm = new WSChannelGroup(context);
        
        for (int i = 0; i < channelNames.length; i++) {
            WSChannel c = context.create(channelNames[i]);
            cm.internalMap.put(channelNames[i], c);
        }
        
        return cm;
    }
    
    @Override
    public CompletableFuture<?> connectAsync() {
        /*List<CompletableFuture<?>> futureList = new ArrayList<>();
        
        for (WSChannel c : internalMap.values()) {
            CompletableFuture<?> future = c.connectAsync();
            futureList.add(future);
        }*/         
        
        return context.connectMultipleAsync(new ArrayList<>(internalMap.values()));
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
        context.closeMultiple(new ArrayList<>(internalMap.values()));
    }
}
