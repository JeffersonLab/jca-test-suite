package org.jlab.jts.caserver;

import static com.cosylab.epics.caj.cas.CAJServerContext.CAJ_SINGLE_THREADED_MODEL;
import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.cas.ServerContext;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.cosylab.epics.caj.cas.util.examples.CounterProcessVariable;
import gov.aps.jca.event.ContextExceptionEvent;
import gov.aps.jca.event.ContextExceptionListener;
import gov.aps.jca.event.ContextVirtualCircuitExceptionEvent;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.jlab.jts.caserver.jmx.ServerKiller;
import org.jlab.jts.caserver.jmx.ServerKillerMBean;

public class CAServer {

    private static final Logger LOGGER = Logger.getLogger(CAServer.class.getName());

    private volatile ServerContext context = null;

    private void initialize() throws CAException {

        System.setProperty("com.cosylab.epics.caj.cas.CAJServerContext.ignore_addr_list", "127.0.0.1:5064 localhost:5064");
        //System.setProperty("com.cosylab.epics.caj.cas.CAJServerContext.beacon_addr_list",  "");
        //System.setProperty("com.cosylab.epics.caj.cas.CAJServerContext.auto_beacon_addr_list",  "false");        

        // Let's slow everything down on purpose!
        //System.setProperty(CAJ_SINGLE_THREADED_MODEL, CAJ_SINGLE_THREADED_MODEL);
        JCALibrary jca = JCALibrary.getInstance();

        DefaultServerImpl server = new DefaultServerImpl();

        //DefaultConfiguration config = new DefaultConfiguration("config");        
        //config.setAttribute("class", JCALibrary.CHANNEL_ACCESS_SERVER_JAVA);
        //context = jca.createServerContext(config, server);        
        context = jca.createServerContext(JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, server);

        registerProcessVariables(server);

        context.addContextExceptionListener(new ContextExceptionListener() {
            @Override
            public void contextException(ContextExceptionEvent ev) {
                LOGGER.log(Level.WARNING, "Context Exception: {0}", ev.getMessage());
                LOGGER.log(Level.WARNING, "Channel: {0}", ev.getChannel() == null ? "Unknown" : ev.getChannel().getName());
            }

            @Override
            public void contextVirtualCircuitException(ContextVirtualCircuitExceptionEvent ev) {
                LOGGER.log(Level.SEVERE, "Context Virtual Circuit Exception: {0}", ev.getStatus());
                LOGGER.log(Level.SEVERE, "Address: {0}", ev.getVirtualCircuit());
            }
        });
    }

    void registerProcessVariables(DefaultServerImpl server) {

        CounterProcessVariable counter = new CounterProcessVariable("counterAlpha", null, 0, Integer.MAX_VALUE, 1, 1000, 10, 20, 0, 100);
        server.registerProcessVaribale(counter);        
        
        final int numCounters = 5000;

        registerCounters(server, numCounters);
    }

    void registerCounters(DefaultServerImpl server, int numCounters) {
        for (int i = 0; i < numCounters; i++) {
            CounterProcessVariable counter = new CounterProcessVariable("counter" + i, null, 0, Integer.MAX_VALUE, 1, 10, 10, 20, 0, 100);
            server.registerProcessVaribale(counter);
        }
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

    static void setupJMX(CAServer server) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        // JMX management object for shutting the server down cleanly
        ServerKillerMBean monitor = new ServerKiller(server);
        MBeanServer mbserver = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("org.jlab.jts.caserver.jmx:type=ServerKiller");
        mbserver.registerMBean(monitor, name);
    }

    public static void main(String[] args) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {

        CAServer server = new CAServer();

        setupJMX(server);

        // Start
        server.execute();
    }

}
