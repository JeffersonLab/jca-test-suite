package org.jlab.caclient.ws;

import java.util.Map;
import org.jlab.caclient.MonitorGroup;

/**
 *
 * @author slominskir
 */
class WSMonitorGroup implements MonitorGroup {

    private final Map<String, WSMonitor> internalMap;    
    
    public WSMonitorGroup(Map<String, WSMonitor> monitorMap) {
        this.internalMap = monitorMap;
    }   
    
    @Override
    public void close() {
        RuntimeException closeException = new RuntimeException("Unable to close monitor");
        for (WSMonitor c : internalMap.values()) {
            try {
                c.close();
            } catch (Exception e) {
                closeException.addSuppressed(e);
            }
        }

        if (closeException.getSuppressed().length > 0) {
            throw closeException;
        }        
    }

}