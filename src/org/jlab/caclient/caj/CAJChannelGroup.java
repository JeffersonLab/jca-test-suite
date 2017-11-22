package org.jlab.caclient.caj;

import com.cosylab.epics.caj.CAJChannel;
import com.cosylab.epics.caj.CAJContext;
import com.cosylab.epics.caj.CAJMonitor;
import gov.aps.jca.CAException;
import gov.aps.jca.dbr.BYTE;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DOUBLE;
import gov.aps.jca.dbr.ENUM;
import gov.aps.jca.dbr.FLOAT;
import gov.aps.jca.dbr.INT;
import gov.aps.jca.dbr.SHORT;
import gov.aps.jca.dbr.STRING;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.MonitorEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.jlab.caclient.ChannelGroup;
import org.jlab.caclient.CustomPrefixThreadFactory;

public class CAJChannelGroup implements ChannelGroup {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new CustomPrefixThreadFactory("CAJ Connect Timeout "));
    private final CAJContext context;
    private final List<CompletableFuture<?>> futureList = new ArrayList<>();
    private final Map<String, CAJChannel> internalMap = new HashMap<>();
    private final String[] channelNames;

    private CAJChannelGroup(CAJContext context, String... channelNames) {
        this.context = context;
        this.channelNames = channelNames;
    }

    public static Object toValue(DBR dbr) {
        Object val;
        if (dbr.isDOUBLE()) {
            DOUBLE record = (DOUBLE) dbr;
            double[] array = record.getDoubleValue();
            if (dbr.getCount() == 1) {
                val = record.getDoubleValue()[0];
            } else {
                val = array;
            }
        } else if (dbr.isFLOAT()) {
            FLOAT record = (FLOAT) dbr;
            float[] array = record.getFloatValue();
            if (dbr.getCount() == 1) {
                val = record.getFloatValue()[0];
            } else {
                val = array;
            }
        } else if (dbr.isINT()) {
            INT record = (INT) dbr;
            int[] array = record.getIntValue();
            if (dbr.getCount() == 1) {
                val = record.getIntValue()[0];
            } else {
                val = array;
            }
        } else if (dbr.isSHORT()) {
            SHORT record = (SHORT) dbr;
            short[] array = record.getShortValue();
            if (dbr.getCount() == 1) {
                val = record.getShortValue()[0];
            } else {
                val = array;
            }
        } else if (dbr.isENUM()) {
            ENUM record = (ENUM) dbr;
            short[] array = record.getEnumValue();
            if (dbr.getCount() == 1) {
                val = record.getEnumValue()[0];
            } else {
                val = array;
            }
        } else if (dbr.isBYTE()) {
            BYTE record = (BYTE) dbr;
            byte[] array = record.getByteValue();
            if (dbr.getCount() == 1) {
                val = record.getByteValue()[0];
            } else {
                val = array;
            }
        } else {
            STRING record = (STRING) dbr;
            String[] array = record.getStringValue();
            if (dbr.getCount() == 1) {
                val = record.getStringValue()[0];
            } else {
                val = array;
            }
        }

        return val;
    }

    public static CAJChannelGroup create(CAJContext context, String... channelNames) {
        return new CAJChannelGroup(context, channelNames);
    }

    @Override
    public CompletableFuture<?> connectAsync() {

        CAException creationException = null;

        try {
            for (int i = 0; i < channelNames.length; i++) {
                CompletableFuture<ConnectionEvent> completable = new CompletableFuture<>();
                ScheduledFuture sf = executor.schedule(() -> {
                    completable.completeExceptionally(new TimeoutException());
                }, 2, TimeUnit.SECONDS);
                futureList.add(completable);
                CAJChannel c = (CAJChannel) context.createChannel(channelNames[i], (ConnectionEvent ev) -> {
                    if (ev.isConnected()) {
                        sf.cancel(false);
                        completable.complete(ev);
                    }
                });
                internalMap.put(channelNames[i], c);
            }
        } catch (CAException e) {
            creationException = e;
        }

        CompletableFuture future = CompletableFuture.allOf(futureList.toArray(new CompletableFuture<?>[0]));

        if (creationException == null) {
            try {
                context.flushIO();
            } catch (CAException e) {
                future.completeExceptionally(e);
            }
        } else {
            future.completeExceptionally(creationException);
        }

        return future;
    }

    public CAJMonitorGroup addValueMonitor(Consumer<? super Object> cnsmr) {
        Map<String, CAJMonitor> monitorMap = new HashMap<>();

        try {
            for (CAJChannel c : internalMap.values()) {
                CAJMonitor monitor = (CAJMonitor) c.addMonitor(c.getFieldType(), c.getElementCount(), gov.aps.jca.Monitor.VALUE, (MonitorEvent ev) -> {
                    cnsmr.accept(CAJChannelGroup.toValue(ev.getDBR()));
                });
                monitorMap.put(c.getName(), monitor);
            }

            context.flushIO();
        } catch (CAException e) {
            throw new RuntimeException("Unable to establish monitors", e);
        }

        return new CAJMonitorGroup(monitorMap);
    }

    public CAJChannel get(String name) {
        return internalMap.get(name);
    }

    public List<CAJChannel> getAll() {
        return new ArrayList<>(internalMap.values());
    }

    @Override
    public void close() throws CAException {
        executor.shutdown();

        CAException closeException = new CAException("Unable to close channel");
        for (CAJChannel c : internalMap.values()) {
            try {
                c.destroy();
            } catch (CAException e) {
                closeException.addSuppressed(e);
            }
        }

        if (closeException.getSuppressed().length > 0) {
            throw closeException;
        }
    }
}
