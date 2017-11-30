package org.jlab.jts.integration.testcase;

import java.util.function.Consumer;
import org.jlab.jts.caclient.CAClient;
import org.jlab.jts.integration.TestCase;
import org.jlab.jts.integration.TestCaseRunner;

/**
 *
 * @author ryans
 */
public class HelloWorldTestCase implements TestCase {

    private final Class<? extends CAClient> clazz;

    public HelloWorldTestCase(Class<? extends CAClient> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void doTest() throws Exception {
        Consumer<? super Object> cnsmr = (value -> System.out.println(value));

        String[] channelNames = {"counter0"};

        try (CAClient client = clazz.newInstance()) {
            new TestCaseRunner(client, 1, 1, cnsmr, channelNames).run();
        }

        System.out.println("Hello World - Done!");
    }

    @Override
    public void close() throws Exception {

    }

}
