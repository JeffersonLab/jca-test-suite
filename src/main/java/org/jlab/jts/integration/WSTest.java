package org.jlab.jts.integration;

import org.jlab.caclient.ws.WSClient;
import org.jlab.jts.integration.testcase.HelloWorldTestCase;

/**
 *
 * @author ryans
 */
public class WSTest {

    public static void main(String[] args) throws Exception {
        try (TestCase test = new HelloWorldTestCase(new WSClient())) {
            test.doTest();
        }
    }
}
