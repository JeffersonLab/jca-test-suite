package org.jlab.jts.caclient.caj;

import com.cosylab.epics.caj.CAJMonitor;
import gov.aps.jca.CAException;
import java.util.Map;
import org.jlab.jts.caclient.MonitorGroup;

/**
 *
 * @author slominskir
 */
public class CAJMonitorGroup implements MonitorGroup {

    private final Map<String, CAJMonitor> internalMap;

    public CAJMonitorGroup(Map<String, CAJMonitor> monitorMap) {
        this.internalMap = monitorMap;
    }

    @Override
    public void close() throws CAException {
        CAException closeException = new CAException("Unable to close monitor");
        for (CAJMonitor c : internalMap.values()) {
            try {
                c.clear();
            } catch (CAException e) {
                closeException.addSuppressed(e);
            }
        }

        if (closeException.getSuppressed().length > 0) {
            throw closeException;
        }        
    }    
    
}
