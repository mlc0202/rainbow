package com.icitic.web.jetty;

import javax.management.MBeanServer;
import com.icitic.core.bundle.BundleActivator;
import com.icitic.core.bundle.BundleException;

public class Activator extends BundleActivator {

	private EmbedJetty embedJetty;

	@Override
    public void start(MBeanServer mBeanServer) throws BundleException {
		super.start(mBeanServer);
		embedJetty = new EmbedJetty();
		try {
			embedJetty.start();
		} catch (Throwable e) {
			throw new BundleException("启动jetty server失败", e);
		}

	}

	@Override
	public void stop() {
		embedJetty.stop();
		super.stop();
	}

}
