package org.jlab.jts.caserver.jmx;

import org.jlab.jts.caserver.CAServer;

/**
 *
 * @author ryans
 */
public class ServerKiller implements ServerKillerMBean {

    private final CAServer server;

    public ServerKiller(CAServer server) {
        this.server = server;
    }

    @Override
    public void stop() {
        System.out.println("Destroying Server");
        server.destroy();
    }

}
