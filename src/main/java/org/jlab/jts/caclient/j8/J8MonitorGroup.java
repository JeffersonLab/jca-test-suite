package org.jlab.jts.caclient.j8;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.epics.ca.Monitor;
import org.jlab.jts.caclient.MonitorGroup;

/**
 *
 * @author slominskir
 */
public class J8MonitorGroup implements MonitorGroup {

    private final Map<String, Monitor<?>> internalMap;

    public J8MonitorGroup(Map<String, Monitor<?>> monitorMap) {
        this.internalMap = monitorMap;
    }

    public Monitor<?> get(String name) {
        return internalMap.get(name);
    }

    public List<Monitor<?>> getAll() {
        return new ArrayList<>(internalMap.values());
    }    
    
    @Override
    public void close() {
        RuntimeException closeException = new RuntimeException("Unable to close monitor");
        for (Monitor<?> c : internalMap.values()) {
            try {
                c.close();
            } catch (RuntimeException e) {
                closeException.addSuppressed(e);
            }
        }

        if (closeException.getSuppressed().length > 0) {
            throw closeException;
        }        
    }

}
