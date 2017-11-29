package org.jlab.jts.integration;

public interface TestCase extends AutoCloseable {
    public void doTest() throws Exception;
}
