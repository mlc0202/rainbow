package com.icitic.core.service.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icitic.core.platform.Session;
import com.icitic.core.service.Request;
import com.icitic.core.service.Response;
import com.icitic.core.service.ServiceInvoker;

public abstract class AbstractBinding implements Binding {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ServiceInvoker serviceInvoker;

    public void setServiceInvoker(ServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    @Override
    public void processRequest(InputStream is, OutputStream os) throws IOException {
        // TODO 要不要考虑多个请求？
        Request request = decodeRequest(is);
        Response response = serviceInvoker.invoke(request);
        encodeResponse(os, response);
        Session.clear();
    }

    protected abstract void encodeResponse(OutputStream os, Response response) throws IOException;

    protected abstract Request decodeRequest(InputStream is) throws IOException;

}
