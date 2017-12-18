package org.jlab.jts.caclient.ws;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.jlab.jts.caclient.CAClient;
import org.jlab.jts.caclient.ChannelGroup;

/**
 *
 * @author slominskir
 */
public class WSClient implements CAClient {
    
    private final Session session;
    private final WSContext context;
    
    private static final Logger LOGGER = Logger.getLogger(
            WSClient.class.getName());
    
    private static final Properties DEFAULT_PROPERTIES = new Properties();
    
    public static final Properties CLIENT_PROPERTIES;
    
    static {
        
        try (InputStream propStream = WSClient.class.getClassLoader().getResourceAsStream("wsclient-default.properties")) {
            if (propStream == null) {
                throw new IOException("Default properties not available");
            }
            DEFAULT_PROPERTIES.load(propStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to load default WSClient properties", e);
        }
        
        CLIENT_PROPERTIES = new Properties(DEFAULT_PROPERTIES);
        
        try (InputStream propStream
                = WSClient.class.getClassLoader().getResourceAsStream(
                        "wsclient.properties")) {
            if (propStream == null) {
                throw new IOException("User properties not available");
            } else {
                
                CLIENT_PROPERTIES.load(propStream);
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "User properties wsclient.properties not found, using defaults");
        }
        
        if(CLIENT_PROPERTIES.getProperty("epics2web_monitor_url") == null) {
            LOGGER.log(Level.SEVERE, "Required property missing: epics2web_monitor_url");
            System.exit(1);
        }
    }
    
    public WSClient() throws URISyntaxException, DeploymentException, IOException {
        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        
        session = container.connectToServer(new Endpoint() {
            @Override
            public void onOpen(Session sn, EndpointConfig ec) {
                System.out.println("Web Socket Session Opened");
            }
            
            @Override
            public void onClose(Session sn, CloseReason reason) {
                System.out.println("Web Socket Session Closed; Reason: " + reason);
            }
            
            @Override
            public void onError(Session sn, Throwable thrwbl) {
                System.err.println("Web Socket Session Error");
                thrwbl.printStackTrace();
            }
        }, new URI(CLIENT_PROPERTIES.getProperty("epics2web_monitor_url")));
        
        context = new WSContext(session);
    }
    
    @Override
    public ChannelGroup create(String[] channelNames) {
        return WSChannelGroup.create(context, channelNames);
    }
    
    @Override
    public void close() throws Exception {
        session.close();
    }
    
}
