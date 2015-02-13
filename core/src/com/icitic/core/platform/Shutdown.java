package com.icitic.core.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.ServiceUnavailableException;

public class Shutdown {

    public static void main(String[] args) {
        String homeStr = System.getProperty("RAINBOW_HOME");
        if (homeStr == null) {
            homeStr = new File(System.getProperty("user.dir")).getParent();
            System.setProperty("RAINBOW_HOME", homeStr);
        }
        File home = new File(homeStr);

        Properties props = new Properties();
        File profile = new File(home, "conf/core.properties");
        try {
            props.load(new FileInputStream(profile));
        } catch (FileNotFoundException e) {
            System.out.println("core.properties not found, use default settings");
        } catch (IOException e) {
            System.out.println("load core properties failed");
            e.printStackTrace();
            return;
        }
        int port = 0;
        try {
            port = Integer.parseInt(props.getProperty("JMX_PORT"));
        } catch (NumberFormatException e) {
            port = 1109;
        }

        System.out.println("Shutting down rainbow platform ...");
        System.out.println("-----------------------------------------------------------------------------"); // NOPMD
        PlatformManagerMBean pmm = getMBean(port);
        if (pmm != null) {
            try {
                pmm.shutdown();
            } catch (Exception e) {
            }
            System.out.println("Rainbow platform is shutted down!");
        }
    }

    private static PlatformManagerMBean getMBean(int port) {
        try {
            StringBuilder sb = new StringBuilder("service:jmx:rmi:///jndi/rmi://127.0.0.1:");
            sb.append(port).append("/").append("rainbow");
            JMXServiceURL url = new JMXServiceURL(sb.toString());
            JMXConnector connector = JMXConnectorFactory.connect(url);
            MBeanServerConnection conn = connector.getMBeanServerConnection();
            return MBeanServerInvocationHandler.newProxyInstance(conn, PlatformManager.getName(),
                PlatformManagerMBean.class, false);
        } catch (Exception e) {
            if (e.getCause() instanceof ServiceUnavailableException) {
                System.out.println("rainbow platform not found!");
            } else {
                System.out.println("get PlatformManager failed");
                e.printStackTrace();
            }
            return null;
        }
    }

}