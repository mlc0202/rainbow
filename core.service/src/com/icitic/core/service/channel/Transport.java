package com.icitic.core.service.channel;

import java.util.List;
import java.util.Map;

import com.icitic.core.model.object.INameObject;

/**
 * 服务传输协议扩展点
 * 
 * @author lijinghui
 * 
 */
public interface Transport extends INameObject {

    /**
     * 返回配置参数名列表
     * 
     * @return
     */
    public List<String> getConfigParamNames();

    /**
     * 创建一个Channel
     * 
     * @param binding
     * @param params
     * @return
     */
    public Server createServer(Map<String, String> params);

}
