package org.jlab.jts.integration.clients;

import org.jlab.jts.caclient.ws.WSClient;
import org.jlab.jts.integration.TestCase;
import org.jlab.jts.integration.testcase.HelloWorldTestCase;
import org.jlab.jts.integration.testcase.HighConcurrencyTestCase;

/**
 *
 * @author ryans
 */
public class WSTest {

    public static void main(String[] args) throws Exception {
        try (TestCase test = new HighConcurrencyTestCase(new WSClient())) {
            test.doTest();
        }
        
        System.exit(0); // Gradle build script will not fully exit without this        
    }
}
