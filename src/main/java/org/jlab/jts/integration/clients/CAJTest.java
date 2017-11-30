package org.jlab.jts.integration.clients;

import org.jlab.jts.caclient.CAClient;
import org.jlab.jts.caclient.caj.CAJClient;
import org.jlab.jts.integration.TestCase;
import org.jlab.jts.integration.testcase.HighConcurrencyTestCase;

/**
 *
 * @author ryans
 */
public class CAJTest {
    public static void main(String[] args) throws Exception {
        Class<? extends CAClient> clazz = CAJClient.class;           
        
        try(TestCase test = new HighConcurrencyTestCase(clazz)){
            test.doTest();
        }
        
        System.exit(0); // Gradle build script will not fully exit without this
    }
}
