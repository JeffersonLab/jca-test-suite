package org.jlab.caclient.j8;

import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.epics.ca.Listener;
import org.epics.ca.Monitor;
import org.epics.ca.Status;
import org.epics.ca.data.Alarm;
import org.epics.ca.data.Control;
import org.epics.ca.data.Graphic;
import org.epics.ca.data.GraphicEnum;
import org.epics.ca.data.Timestamped;
import org.jlab.caclient.CAClient;
import org.jlab.caclient.ChannelGroup;

public class J8Client implements CAClient {

    private final Context context;
    
    public J8Client() {

        Properties properties = new Properties();

        //properties.setProperty(Context.Configuration.EPICS_CA_ADDR_LIST.toString(), "127.0.0.1");
        //properties.setProperty(Context.Configuration.EPICS_CA_ADDR_LIST.toString(), "129.57.95.151");
        properties.setProperty(Context.Configuration.EPICS_CA_AUTO_ADDR_LIST.toString(), "false");
        //properties.setProperty(Context.Configuration.EPICS_CA_SERVER_PORT.toString(), "5064");
        // SYSTEM properties are different than Context properites
        System.setProperty("org.epics.ca.impl.reactor.lf.LeaderFollowersThreadPool.thread_pool_size", "2");

        context = new Context(properties);
    }

    @SuppressWarnings({"unused"})
    public static void main(String[] args) {
        try (CAClient client = new J8Client()) {

        } catch (Exception e) {
            System.err.println("Caught an exception");
            e.printStackTrace();
        }
    }

    private void doTest3() throws InterruptedException, ExecutionException, TimeoutException {

        AtomicLong updates = new AtomicLong();

        final int numCounters = 100;
        final int secondsToSleep = 20;
        String[] channelNames = new String[numCounters];

        for (int i = 0; i < channelNames.length; i++) {
            channelNames[i] = "counter" + i;
        }
        
        long start = 0;
        try (J8ChannelGroup channels = J8ChannelGroup.create(context, channelNames)) {
            System.out.println("Connecting to channels");
            channels.connectAsync().get(2, TimeUnit.SECONDS);

            Consumer<? super Object> c = (value -> updates.incrementAndGet());
            //Consumer<? super Object> c = (value -> System.out.println(value.getClass()));

            start = System.currentTimeMillis();
            try (J8MonitorGroup monitors = channels.addValueMonitor(c)) {
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

    private void doTest2(Context context) throws InterruptedException, ExecutionException, TimeoutException {
        try (Channel<Integer> channel = context.createChannel("counter1", Integer.class);) {
            System.out.println("Connecting to channel");
            channel.connectAsync().get(1, TimeUnit.SECONDS);

            try (Monitor<Integer> monitor = channel.addValueMonitor(value -> System.out.println(value))) {
                Thread.sleep(60000);
            }
        }

        System.out.println("done with test");
    }

    private void doTest1(Context context) throws InterruptedException, ExecutionException {
        Channel<Double> adc = context.createChannel("adc01", Double.class);

        // add connection listener
        Listener cl = adc.addConnectionListener((channel, state) -> System.out.println(channel.getName() + " is connected? " + state));
        // remove listener, or use try-catch-resources
        //cl.close();	

        Listener cl2 = adc.addAccessRightListener((channel, rights) -> System.out.println(channel.getName() + " is rights? " + rights));

        // wait until connected
        adc.connectAsync().get();

        adc.putNoWait(3.11);

        CompletableFuture<Status> fp = adc.putAsync(12.8);
        System.out.println(fp.get());

        CompletableFuture<Double> ffd = adc.getAsync();
        System.out.println(ffd.get());

        CompletableFuture<Alarm<Double>> fts = adc.getAsync(Alarm.class);
        Alarm<Double> da = fts.get();
        System.out.println(da.getValue() + " " + da.getAlarmStatus() + " " + da.getAlarmSeverity());

        CompletableFuture<Timestamped<Double>> ftt = adc.getAsync(Timestamped.class);
        Timestamped<Double> dt = ftt.get();
        System.out.println(dt.getValue() + " " + dt.getAlarmStatus() + " " + dt.getAlarmSeverity() + " " + new Date(dt.getMillis()));

        CompletableFuture<Graphic<Double, Double>> ftg = adc.getAsync(Graphic.class);
        Graphic<Double, Double> dg = ftg.get();
        System.out.println(dg.getValue() + " " + dg.getAlarmStatus() + " " + dg.getAlarmSeverity());

        CompletableFuture<Control<Double, Double>> ftc = adc.getAsync(Control.class);
        Control<Double, Double> dc = ftc.get();
        System.out.println(dc.getValue() + " " + dc.getAlarmStatus() + " " + dc.getAlarmSeverity());

        /*
			Channel<double[]> adca = context.createChannel("msekoranjaHost:compressExample", double[].class).connectAsync().get();

			CompletableFuture<double[]> ffda = adca.getAsync();
			System.out.println(Arrays.toString(ffda.get()));

			CompletableFuture<Graphic<double[], Double>> ftga = adca.getAsync(Graphic.class);
			Graphic<double[], Double> dga = ftga.get();
			System.out.println(Arrays.toString(dga.getValue()) + " " + dga.getAlarmStatus() + " " + dga.getAlarmSeverity());
         */
        Channel<Short> ec = context.createChannel("enum", Short.class).connectAsync().get();

        CompletableFuture<Short> fec = ec.getAsync();
        System.out.println(fec.get());

        Short s = ec.get();
        System.out.println(s);

        CompletableFuture<GraphicEnum> ftec = ec.getAsync(GraphicEnum.class);
        GraphicEnum dtec = ftec.get();
        System.out.println(dtec.getValue() + " " + Arrays.toString(dtec.getLabels()));

        GraphicEnum ss = ec.get(GraphicEnum.class);
        System.out.println(Arrays.toString(ss.getLabels()));

        ec.putNoWait((short) (s + 1));

        Monitor<Double> mon = adc.addValueMonitor(value -> System.out.println(value));
        //Thread.sleep(10000);
        //mon.close();

        Monitor<Timestamped<Double>> mon2
                = adc.addMonitor(
                        Timestamped.class,
                        value -> {
                            if (value != null) {
                                System.out.println(new Date(value.getMillis()) + " / " + value.getValue());
                            }
                        }
                );

        Thread.sleep(5000);

        if (true) {
            return;
        }

        // sync create channel and connect
        Channel<Double> adc4 = context.createChannel("adc02", Double.class).connectAsync().get();

        // async wait
        // NOTE: thenAccept vs thenAcceptAsync
        adc.connectAsync().thenAccept((channel) -> System.out.println(channel.getName() + " connected"));

        Channel<Integer> adc2 = context.createChannel("adc02", Integer.class);
        Channel<String> adc3 = context.createChannel("adc03", String.class);

        // wait for all channels to connect
        CompletableFuture.allOf(adc2.connectAsync(), adc3.connectAsync()).
                thenAccept((v) -> System.out.println("all connected"));

        // sync get
        double dv = adc.get();

        // sync get w/ timestamp 
        Timestamped<Double> ts = adc.get(Timestamped.class);
        dv = ts.getValue();
        long millis = ts.getMillis();

        // best-effort put
        adc.putNoWait(12.3);

        // async get
        CompletableFuture<Double> fd = adc.getAsync();
        // ... in some other thread
        dv = fd.get();

        CompletableFuture<Timestamped<Double>> ftd = adc.getAsync(Timestamped.class);
        // ... in some other thread
        Timestamped<Double> td = ftd.get();

        CompletableFuture<Status> sf = adc.putAsync(12.8);
        boolean putOK = sf.get().isSuccessful();

        // create monitor
        Monitor<Double> monitor = adc.addValueMonitor(value -> System.out.println(value));
        monitor.close();	// try-catch-resource can be used

        Monitor<Timestamped<Double>> monitor2
                = adc.addMonitor(
                        Timestamped.class,
                        value -> System.out.println(value)
                );
    }

    @Override
    public ChannelGroup create(String[] channelNames) {
        return J8ChannelGroup.create(context, channelNames);
    }

    @Override
    public void close() throws Exception {
        context.close();
    }
}
