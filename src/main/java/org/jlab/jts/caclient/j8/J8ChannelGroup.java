package org.jlab.jts.caclient.j8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.epics.ca.Channel;
import org.epics.ca.ChannelDescriptor;
import org.epics.ca.Context;
import org.epics.ca.Monitor;
import org.jlab.jts.caclient.ChannelGroup;

public class J8ChannelGroup implements ChannelGroup {
    
    private final Map<String, Channel<?>> internalMap = new HashMap<>();
    
    public static J8ChannelGroup create(Context context, String... channelNames) {
        
        ChannelDescriptor[] descriptors = new ChannelDescriptor[channelNames.length];
        
        for (int i = 0; i < channelNames.length; i++) {
            descriptors[i] = new ChannelDescriptor<>(channelNames[i], Object.class);
        }
        
        return J8ChannelGroup.create(context, descriptors);
    }
    
    public static J8ChannelGroup create(Context context, ChannelDescriptor<?>... descriptors) {
        J8ChannelGroup cm = new J8ChannelGroup();
        
        for (ChannelDescriptor<?> descriptor : descriptors) {
            Channel<?> c = context.createChannel(descriptor.getName(), descriptor.getType());
            cm.internalMap.put(descriptor.getName(), c);
        }
        
        return cm;
    }
    
    public CompletableFuture<?> connectAsync() {
        List<CompletableFuture<?>> futureList = new ArrayList<>();
        
        for (Channel<?> c : internalMap.values()) {
            CompletableFuture<?> future = c.connectAsync();
            futureList.add(future);
        }        
        
        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture<?>[0]));
    }
    
    public J8MonitorGroup addValueMonitor(Consumer<? super Object> cnsmr) {
        Map<String, Monitor<?>> monitorMap = new HashMap<>();
        
        for (Channel<?> c : internalMap.values()) {
            Monitor<?> monitor = c.addValueMonitor(cnsmr);
            monitorMap.put(c.getName(), monitor);
        }        
        
        return new J8MonitorGroup(monitorMap);        
    }
    
    public Channel<?> get(String name) {
        return internalMap.get(name);
    }
    
    public List<Channel<?>> getAll() {
        return new ArrayList<>(internalMap.values());
    }
    
    @Override
    public void close() {
        RuntimeException closeException = new RuntimeException("Unable to close channel");
        for (Channel<?> c : internalMap.values()) {
            try {
                c.close();
            } catch (RuntimeException e) {
                closeException.addSuppressed(e);
            }
        }
        
        if (closeException.getSuppressed().length > 0) {
            throw closeException;
        }
    }
}
