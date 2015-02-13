package com.icitic.core.service.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.icitic.core.model.object.INameObject;
import com.icitic.core.platform.Session;

/**
 * 服务编码绑定扩展点，Binding应该负责设置 {@link Session}
 * 
 * @author lijinghui
 * 
 */
public interface Binding extends INameObject {

    void processRequest(InputStream is, OutputStream os) throws IOException;

}
