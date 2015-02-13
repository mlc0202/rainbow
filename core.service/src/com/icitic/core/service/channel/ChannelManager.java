package com.icitic.core.service.channel;

import java.util.Collection;

/**
 * 通道管理器接口
 * 
 * @author lijinghui
 * 
 */
public interface ChannelManager {

	/**
	 * 返回所有的通道
	 * 
	 * @return
	 */
	Collection<Channel> getChannels();

	/**
	 * 返回指定名字的通道
	 * 
	 * @param name
	 * @return
	 */
	Channel getChannel(String name);

	/**
	 * 创建一个通道
	 * 
	 * @param name
	 * @param binding
	 * @param transport
	 * @param config
	 * @throws Exception
	 */
	void createChannel(String name, String binding, String transport,
			String config) throws Exception;

	/**
	 * 删除一个通道
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	boolean removeChannel(String name) throws Exception;

	/**
	 * 打开一个通道
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	boolean openChannel(String name) throws Exception;

	/**
	 * 关闭一个通道
	 * 
	 * @param name
	 * @return
	 */
	boolean closeChannel(String name);

}
