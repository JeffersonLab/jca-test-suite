package org.jlab.jts.caclient.ws;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

    public WSClient() throws URISyntaxException, DeploymentException, IOException {
        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        session = container.connectToServer(new Endpoint() {
            @Override
            public void onOpen(Session sn, EndpointConfig ec) {
                //System.out.println("Opened!");
            }

            @Override
            public void onClose(Session sn, CloseReason reason) {
                //System.out.println("Closed: " + reason);
            }

            @Override
            public void onError(Session sn, Throwable thrwbl) {
                System.err.println("Error!");
                thrwbl.printStackTrace();
            }
        }, new URI("ws://127.0.0.1:8084/epics2web/monitor"));

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
