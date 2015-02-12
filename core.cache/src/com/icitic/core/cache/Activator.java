package com.icitic.core.cache;

import java.util.Map;

import javax.management.MBeanServer;

import com.google.common.collect.ImmutableMap;
import com.icitic.core.bundle.BundleException;
import com.icitic.core.cache.internal.CacheManagerImpl;
import com.icitic.core.service.DefaultBundleActivator;
import com.icitic.core.service.InjectProvider;
import com.icitic.core.util.ioc.Bean;

public class Activator extends DefaultBundleActivator {

    @Override
    public Map<String, Bean> getContextConfig() {
        return ImmutableMap.of("cacheManager", Bean.singleton(CacheManagerImpl.class));
    }

    @Override
    public void start(MBeanServer mBeanServer) throws BundleException {
        super.start(mBeanServer);
        registerExtension(InjectProvider.class, "cacheManager");
    }

}
