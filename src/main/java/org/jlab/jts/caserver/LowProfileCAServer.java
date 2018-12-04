package org.jlab.jts.caserver;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

/**
 * Uses only 5 counters instead of 5000 as to avoid putting too much server load
 * on localhost.
 *
 * @author ryans
 */
public class LowProfileCAServer extends CAServer {
    
    @Override
    void registerProcessVariables(DefaultServerImpl server) {     
        
        final int numCounters = 5;

        registerCounters(server, numCounters);
    }    
    
    public static void main(String[] args) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {

        CAServer server = new LowProfileCAServer();

        setupJMX(server);

        // Start
        server.execute();
    }
}
