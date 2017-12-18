package org.jlab.jts.caclient.j8;

import java.util.Properties;
import org.epics.ca.Context;
import org.jlab.jts.caclient.CAClient;
import org.jlab.jts.caclient.ChannelGroup;

public class J8Client implements CAClient {

    private final Context context;
    
    public J8Client() {

        Properties properties = new Properties();

        properties.setProperty(Context.Configuration.EPICS_CA_ADDR_LIST.toString(), "127.0.0.1");
        properties.setProperty(Context.Configuration.EPICS_CA_AUTO_ADDR_LIST.toString(), "NO");

        //properties.setProperty("CA_DEBUG", "1");
        
        // SYSTEM properties are different than Context properites
        //System.setProperty("org.epics.ca.impl.reactor.lf.LeaderFollowersThreadPool.thread_pool_size", "2");

        context = new Context(properties);
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
