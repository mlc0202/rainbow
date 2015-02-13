package com.icitic.core.service.channel;

import static com.google.common.base.Preconditions.*;
import com.icitic.core.extension.ExtensionRegistry;
import com.icitic.core.model.object.SimpleNameObject;

/**
 * 
 * 服务通道，是一个Binding与一个Transport组合
 * 
 * @author lijinghui
 * 
 */
public class Channel extends SimpleNameObject {

    private String bindingName;

    private Server server;

    public String getName() {
        return name;
    }

    public boolean isOpened() {
        return server.isRunning();
    }

    synchronized public void open() {
        if (isOpened())
            return;
        Binding binding = ExtensionRegistry.getExtensionObject(Binding.class, bindingName);
        checkNotNull(binding, "binding[%s] not online", bindingName);
        server.start(binding);
    }

    synchronized public void close() {
        if (isOpened()) {
            server.stop();
        }
    }

}
