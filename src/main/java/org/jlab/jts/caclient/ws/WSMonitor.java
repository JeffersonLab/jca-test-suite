package org.jlab.jts.caclient.ws;

import java.util.function.Consumer;

/**
 *
 * @author slominskir
 */
public class WSMonitor implements AutoCloseable{

    private final Consumer<? super Object> cnsmr;
    private volatile boolean open = true;
    
    WSMonitor(Consumer<? super Object> cnsmr) {
        this.cnsmr = cnsmr;
    }

    @Override
    public void close() throws Exception {
        open = false;
    }

    void update(Object value) {
        if(open) { // Don't count updates after monitor close called
            cnsmr.accept(value);
        }
    }
    
}
