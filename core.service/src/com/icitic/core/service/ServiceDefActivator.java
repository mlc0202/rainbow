package com.icitic.core.service;

import java.util.LinkedList;
import java.util.List;

import javax.management.MBeanServer;

import com.icitic.core.bundle.BundleActivator;
import com.icitic.core.bundle.BundleClassLoader;
import com.icitic.core.bundle.BundleException;
import com.icitic.core.bundle.Resource;
import com.icitic.core.bundle.ResourceProcessor;
import com.icitic.core.util.Utils;

public abstract class ServiceDefActivator extends BundleActivator {

    private List<String> services = null;

    /**
     * Bundle启动
     * 
     * @param mBeanServer
     * @param bundleId
     * @throws BundleException
     */
    @Override
    public void start(MBeanServer mBeanServer) throws BundleException {
        super.start(mBeanServer);
        services = new LinkedList<String>();
        getClassLoader().procResource(new ResourceProcessor() {
            @Override
            public void processResource(BundleClassLoader classLoader, Resource resource) throws BundleException {
                if (resource.getName().endsWith("Service.class")) {
                    String className = resource.getName().replace('/', '.').replace(".class", Utils.NULL_STR);
                    try {
                        Class<?> serviceClass = classLoader.loadClass(className);
                        String serviceId = Activator.getServiceRegistry().registerService(serviceClass);
                        services.add(serviceId);
                    } catch (ClassNotFoundException e) {
                        throw new BundleException(e.toString());
                    }
                }
            }
        });
    }

    protected final void registerService(Class<?> serviceClass) {
    }

    @Override
    public void stop() {
        if (services != null)
            for (String serviceId : services) {
                Activator.getServiceRegistry().unregisterService(serviceId);
            }
        super.stop();
    }

}
