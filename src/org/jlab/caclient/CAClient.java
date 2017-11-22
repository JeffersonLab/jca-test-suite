package org.jlab.caclient;

/**
 *
 * @author slominskir
 */
public interface CAClient extends AutoCloseable {

    public ChannelGroup create(String[] channelNames);
    
}
