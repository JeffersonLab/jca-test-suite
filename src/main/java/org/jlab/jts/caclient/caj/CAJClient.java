package org.jlab.jts.caclient.caj;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.DefaultConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.jts.caclient.CAClient;
import org.jlab.jts.caclient.ChannelGroup;

/**
 *
 * @author slominskir
 */
public class CAJClient implements CAClient {

    private final CAJContext context;

    private static final Logger LOGGER = Logger.getLogger(
            CAJClient.class.getName());

    private static final DefaultConfiguration CAJ_CONFIG = new DefaultConfiguration("config");

    private static final Properties DEFAULT_PROPERTIES = new Properties();

    public static final Properties CLIENT_PROPERTIES;

    static {

        try (InputStream propStream = CAJClient.class.getClassLoader().getResourceAsStream("cajclient-default.properties")) {
            if (propStream == null) {
                throw new IOException("Default properties not available");
            }
            DEFAULT_PROPERTIES.load(propStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to load default CAJClient properties", e);
        }       
        
        CLIENT_PROPERTIES = new Properties(DEFAULT_PROPERTIES);

        try (InputStream propStream
                = CAJClient.class.getClassLoader().getResourceAsStream(
                        "cajclient.properties")) {
            if (propStream == null) {
                throw new IOException("User properties not available");
            } else {

                CLIENT_PROPERTIES.load(propStream);
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "User properties cajclient.properties not found, using defaults");
        }

        CAJ_CONFIG.setAttribute("class", JCALibrary.CHANNEL_ACCESS_JAVA);

        if (CLIENT_PROPERTIES.getProperty("addr_list") != null) {
            CAJ_CONFIG.setAttribute("addr_list", (String) CLIENT_PROPERTIES.getProperty("addr_list"));
        }

        if (CLIENT_PROPERTIES.getProperty("auto_addr_list") != null) {
            CAJ_CONFIG.setAttribute("auto_addr_list", (String) CLIENT_PROPERTIES.getProperty("auto_addr_list"));
        }

        if (CLIENT_PROPERTIES.getProperty("connection_timeout") != null) {
            CAJ_CONFIG.setAttribute("connection_timeout", (String) CLIENT_PROPERTIES.getProperty("connection_timeout"));
        }

        if (CLIENT_PROPERTIES.getProperty("beacon_period") != null) {
            CAJ_CONFIG.setAttribute("beacon_period", (String) CLIENT_PROPERTIES.getProperty("beacon_period"));
        }

        if (CLIENT_PROPERTIES.getProperty("repeater_port") != null) {
            CAJ_CONFIG.setAttribute("repeater_port", (String) CLIENT_PROPERTIES.getProperty("repeater_port"));
        }

        if (CLIENT_PROPERTIES.getProperty("server_port") != null) {
            CAJ_CONFIG.setAttribute("server_port", (String) CLIENT_PROPERTIES.getProperty("server_port"));
        }

        if (CLIENT_PROPERTIES.getProperty("max_array_bytes") != null) {
            CAJ_CONFIG.setAttribute("max_array_bytes", (String) CLIENT_PROPERTIES.getProperty("max_array_bytes"));
        }
    }

    public CAJClient() throws CAException, InterruptedException, ExecutionException, TimeoutException {
        JCALibrary jca = JCALibrary.getInstance();

        context = (CAJContext) jca.createContext(CAJ_CONFIG);

        context.initialize();

        //context.printInfo();
    }

    @Override
    public ChannelGroup create(String[] channelNames) {
        return CAJChannelGroup.create(context, channelNames);
    }

    @Override
    public void close() throws Exception {
        context.destroy();
    }
}
