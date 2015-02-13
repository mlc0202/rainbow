package com.icitic.core.service.internal;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.icitic.core.bundle.BundleListener;
import com.icitic.core.model.object.SimpleNameObject;
import com.icitic.core.service.channel.Channel;
import com.icitic.core.service.channel.ChannelManager;
import com.icitic.core.util.ioc.DisposableBean;

public final class ChannelManagerImpl extends SimpleNameObject implements ChannelManager, BundleListener, DisposableBean {

//	private ServiceRegistry serviceRegistry;

	private List<Channel> channels = new CopyOnWriteArrayList<Channel>();

	public static ChannelManager instance = new ChannelManagerImpl();

	private ChannelManagerImpl() {
	}

    // @Inject
    // public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    // this.serviceRegistry = serviceRegistry;
    // }

	@Override
	public Collection<Channel> getChannels() {
		return channels;
	}

	@Override
	public void createChannel(String name, String binding, String transport, String config) {
//		if (getChannel(name) != null)
//			throw new IllegalArgumentException("channel already exist:" + name);
//		Extension<Binding> b = ExtensionRegistry.getExtension(Binding.class, binding);
//		if (b == null)
//			throw new IllegalArgumentException("binding not found:" + binding);
//		Extension<Transport> t = ExtensionRegistry.getExtension(Transport.class, transport);
//		if (t == null)
//			throw new IllegalArgumentException("transport not found:" + transport);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.icitic.core.service.channel.ChannelManager#getChannel(java.lang.String
	 * )
	 */
	@Override
	public Channel getChannel(String name) {
		for (Channel c : channels) {
			if (c.getName().equals(name))
				return c;
		}
		return null;
	}

	@Override
	public boolean openChannel(String name) throws Exception {
		Channel c = getChannel(name);
		if (c == null)
			return false;
		c.open();
		return true;
	}

	@Override
	public boolean closeChannel(String name) {
		Channel c = getChannel(name);
		if (c == null)
			return false;
		c.close();
		return true;
	}

	@Override
	public void destroy() {
		for (Channel c : channels) {
			c.close();
		}
		channels.clear();
	}

	@Override
	public boolean removeChannel(String name) {
		Channel c = getChannel(name);
		if (c == null)
			return false;
		c.close();
		return channels.remove(c);
	}

	public void removeChannel(List<Channel> kill) {
		for (Channel c : kill) {
			c.close();
		}
		channels.removeAll(kill);
	}

	@Override
	public void bundleStarted(String id) {

	}

	@Override
	public void bundleStopping(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bundleStop(String id) {
		// TODO Auto-generated method stub

	}

}
