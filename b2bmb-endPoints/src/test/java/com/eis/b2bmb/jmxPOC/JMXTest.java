package com.eis.b2bmb.jmxPOC;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class JMXTest {
    public static void main(String[] args) throws Exception {
        ApplicationCache cache = new ApplicationCache();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("org.javalobby.tnt.jmx:type=ApplicationCacheMBean");
        mbs.registerMBean(cache, name);
        imitateActivity(cache);
    }

    private static void imitateActivity(ApplicationCache cache) {
        while (true) {
            try {
                cache.cacheObject(new Object());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}