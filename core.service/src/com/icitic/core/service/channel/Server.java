package com.icitic.core.service.channel;

/**
 * 传输协议响应服务器
 */
public interface Server {

    /**
     * 启动服务器
     * 
     * @throws Exception
     */
    public void start(Binding binding);

    /**
     * 停止服务器
     */
    public void stop();

    /**
     * 服务器状态
     * 
     * @return
     */
    public boolean isRunning();
    
}
