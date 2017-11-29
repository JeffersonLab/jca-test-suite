package org.jlab.jts.integration;

import org.jlab.caclient.caj.CAJClient;
import org.jlab.jts.integration.testcase.HighThroughputTestCase;

/**
 *
 * @author ryans
 */
public class CAJTest {
    public static void main(String[] args) throws Exception {
        try(TestCase test = new HighThroughputTestCase(new CAJClient())){
            test.doTest();
        }
    }
}
