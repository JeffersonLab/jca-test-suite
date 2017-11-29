package org.jlab.caclient.caj;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.DefaultConfiguration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.jlab.caclient.CAClient;
import org.jlab.caclient.ChannelGroup;

/**
 *
 * @author slominskir
 */
public class CAJClient implements CAClient {

    private final CAJContext context;
    
    public CAJClient() throws CAException, InterruptedException, ExecutionException, TimeoutException {
        JCALibrary jca = JCALibrary.getInstance();

        DefaultConfiguration config = new DefaultConfiguration("config");

        config.setAttribute("class", JCALibrary.CHANNEL_ACCESS_JAVA);
        //config.setAttribute("addr_list", "localhost");
        //config.setAttribute("auto_addr_list", "false");

        context = (CAJContext) jca.createContext(config);

        context.initialize();
    }

    @Override
    public ChannelGroup create(String[] channelNames) {
        return CAJChannelGroup.create(context, channelNames);
    }

    @Override
    public void close() throws Exception {
        context.dispose();
    }
}
