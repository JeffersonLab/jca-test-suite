package org.jlab.jts.caclient.ws;

import java.util.function.Consumer;

/**
 *
 * @author slominskir
 */
public class WSMonitor implements AutoCloseable{

    private final Consumer<? super Object> cnsmr;
    
    WSMonitor(Consumer<? super Object> cnsmr) {
        this.cnsmr = cnsmr;
    }

    @Override
    public void close() throws Exception {

    }

    void update(Object value) {
        cnsmr.accept(value);
    }
    
}
