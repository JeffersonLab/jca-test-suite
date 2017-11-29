package org.jlab.jts.caserver;

import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.cas.ServerContext;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.cosylab.epics.caj.cas.util.examples.CounterProcessVariable;
import java.io.IOException;

public class CAServer {

    private volatile ServerContext context = null;

    private void initialize() throws CAException {

        System.setProperty("com.cosylab.epics.caj.cas.CAJServerContext.ignore_addr_list",  "127.0.0.1:5064 localhost:5064");   
        //System.setProperty("com.cosylab.epics.caj.cas.CAJServerContext.beacon_addr_list",  "");
        //System.setProperty("com.cosylab.epics.caj.cas.CAJServerContext.auto_beacon_addr_list",  "false");        
        
        JCALibrary jca = JCALibrary.getInstance();

        DefaultServerImpl server = new DefaultServerImpl();
        
        //DefaultConfiguration config = new DefaultConfiguration("config");        
        //config.setAttribute("class", JCALibrary.CHANNEL_ACCESS_SERVER_JAVA);
        //context = jca.createServerContext(config, server);        
        
        context = jca.createServerContext(JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, server);

        registerProcessVariables(server);
    }

    private void registerProcessVariables(DefaultServerImpl server) {

        final int numCounters = 100;

        for (int i = 0; i < numCounters; i++) {
            CounterProcessVariable counter = new CounterProcessVariable("counter" + i, null, 0, Integer.MAX_VALUE, 1, 10, 10, 20, 0, 100);
            server.registerProcessVaribale(counter);
        }

        CounterProcessVariable counter = new CounterProcessVariable("counterAlpha", null, 0, Integer.MAX_VALUE, 1, 1000, 10, 20, 0, 100);
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

            //System.out.println("Done.");
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            System.out.println("Shutting down...");
            destroy();
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
