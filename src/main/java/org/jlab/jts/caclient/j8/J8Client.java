package org.jlab.jts.caclient.j8;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.ca.Context;
import org.jlab.jts.caclient.CAClient;
import org.jlab.jts.caclient.ChannelGroup;

public class J8Client implements CAClient {

    private final Context context;

    private static final Logger LOGGER = Logger.getLogger(
            J8Client.class.getName());

    private static final Properties DEFAULT_PROPERTIES = new Properties();

    public static final Properties CLIENT_PROPERTIES;

    static {

        try (InputStream propStream = J8Client.class.getClassLoader().getResourceAsStream("j8client-default.properties")) {
            if (propStream == null) {
                throw new IOException("Default properties not available");
            }
            DEFAULT_PROPERTIES.load(propStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to load default J8Client properties", e);
        }

        CLIENT_PROPERTIES = new Properties(DEFAULT_PROPERTIES);

        try (InputStream propStream
                = J8Client.class.getClassLoader().getResourceAsStream(
                        "j8client.properties")) {
            if (propStream == null) {
                throw new IOException("User properties not available");
            } else {

                CLIENT_PROPERTIES.load(propStream);
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "User properties j8client.properties not found, using defaults");
        }

        // This is a convenience to avoid having to set -Dorg.epics.ca.impl.reactor.lf.LeaderFollowersThreadPool.thread_pool_size on the java command line
        if (CLIENT_PROPERTIES.get("org.epics.ca.impl.reactor.lf.LeaderFollowersThreadPool.thread_pool_size") != null) {
            System.setProperty("org.epics.ca.impl.reactor.lf.LeaderFollowersThreadPool.thread_pool_size",
                    (String) CLIENT_PROPERTIES.get("org.epics.ca.impl.reactor.lf.LeaderFollowersThreadPool.thread_pool_size"));
        }
    }

    public J8Client() {
        context = new Context(CLIENT_PROPERTIES);
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
