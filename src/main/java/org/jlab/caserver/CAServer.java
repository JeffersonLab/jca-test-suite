package org.jlab.caserver;

import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.cas.ServerContext;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Int;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;
import com.cosylab.epics.caj.cas.util.examples.CounterProcessVariable;
import java.io.IOException;

public class CAServer {

    private volatile ServerContext context = null;

    private void initialize() throws CAException {

        JCALibrary jca = JCALibrary.getInstance();

        DefaultServerImpl server = new DefaultServerImpl();

        context = jca.createServerContext(JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, server);

        registerProcessVariables(server);
    }

    private void registerProcessVariables(DefaultServerImpl server) {

        final int numCounters = 100;

        for (int i = 0; i < numCounters; i++) {
            CounterProcessVariable counter = new CounterProcessVariable("counter" + i, null, 0, 1000, 1, 1, -7, 7, -9, 9);
            server.registerProcessVaribale(counter);
        }

        CounterProcessVariable counter = new CounterProcessVariable("counterAlpha", null, -10, 10, 1, 1000, -7, 7, -9, 9);
        server.registerProcessVaribale(counter);
    }

    public void destroy() {
        try {
            if (context != null) {
                context.destroy();
            }

        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void execute() {
        try {
            initialize();

            System.out.println(context.getVersion().getVersionString());
            context.printInfo();
            System.out.println();

            System.out.println("Running server...");

            // run server 
            context.run(0);

            System.out.println("Done.");

        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            System.out.println("Shutting down...");
            destroy();
        }

    }

    public void runInSeparateThread() {
        try {

            // initialize context
            initialize();

            // run server 
            new Thread(() -> {
                try {
                    context.run(0);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }).start();

        } catch (Throwable th) {
            throw new RuntimeException("Failed to start CA server.", th);
        }
    }

    public static void main(String[] args) {
        CAServer server = new CAServer();

        Thread shutdownThread = new Thread() {
            public void run() {
                System.out.println("hit enter key to shutdown");
                try {
                    System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.destroy();
                //System.exit(0);                
            }
        };

        shutdownThread.start();

        server.execute();
    }

}
