package org.jlab.jts.caclient;

import org.jlab.jts.caclient.caj.CAJClient;
import org.jlab.jts.caclient.j8.J8Client;
import org.jlab.jts.caclient.ws.WSClient;
import org.jlab.jts.integration.TestCase;
import org.jlab.jts.integration.testcase.HelloWorldTestCase;
import org.jlab.jts.integration.testcase.HighConcurrencyTestCase;
import org.jlab.jts.integration.testcase.HighThroughputTestCase;

/**
 *
 * @author slominskir
 */
public interface CAClient extends AutoCloseable {
    
    public ChannelGroup create(String[] channelNames);
    
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, Exception {
        if (args != null && args.length == 2) {
            String clientStr = args[0];
            String testStr = args[1];
            System.out.println("Client: " + clientStr);
            System.out.println("Test: " + testStr);
            
            Class<? extends CAClient> clientClazz;
            Class<? extends TestCase> testClazz;
            
            switch (clientStr) {
                case "caj":
                    clientClazz = CAJClient.class;
                    break;
                case "j8":
                    clientClazz = J8Client.class;
                    break;
                case "ws":
                    clientClazz = WSClient.class;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown client: " + clientStr);
            }
            
            switch (testStr) {
                case "hello":
                    testClazz = HelloWorldTestCase.class;
                    break;
                case "throughput":
                    testClazz = HighThroughputTestCase.class;
                    break;
                case "concurrency":
                    testClazz = HighConcurrencyTestCase.class;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown test: " + testStr);
            }
            
            Class[] constructorArgs = {Class.class};
            
            try (TestCase test = testClazz.getDeclaredConstructor(constructorArgs).newInstance(clientClazz)) {
                test.doTest();
            }
            
        } else {
            throw new IllegalArgumentException("Please provide 2 arguments: (0) Client, (1) Test");
        }
    }
}
