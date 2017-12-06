package org.jlab.jts.caserver;

import org.jlab.jts.caserver.jmx.ServerKillerMBean;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * @author ryans
 */
public class StopServer {

    public static void main(String[] args) throws MalformedURLException, IOException, MalformedObjectNameException {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
        try (JMXConnector jmxc = JMXConnectorFactory.connect(url, null)) {
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
            ObjectName mbeanName = new ObjectName("org.jlab.jts.caserver.jmx:type=ServerKiller");
            ServerKillerMBean mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, ServerKillerMBean.class, true);
            mbeanProxy.stop();
        }
    }
}
