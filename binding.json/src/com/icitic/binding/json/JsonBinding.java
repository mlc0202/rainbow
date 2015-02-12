package com.icitic.binding.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.icitic.core.platform.Session;
import com.icitic.core.service.Request;
import com.icitic.core.service.Response;
import com.icitic.core.service.channel.AbstractBinding;
import com.icitic.core.util.Utils;

/**
 * 基于Json的绑定协议。
 * 
 * 请求的格式，Session内容key，value各占一行，空行表示session结束；Service，method，param各占一行
 * 
 * @author lijinghui
 * 
 */
public class JsonBinding extends AbstractBinding {

	@Override
	public String getName() {
		return "json";
	}

	@Override
	public void encodeResponse(OutputStream os, Response response) throws IOException {
		// TODO 没有做好，要考虑重建exception
		String result;
		if (response.hasException())
			result = JSON.toJSONString(response.getException());
		else
			result = JSON.toJSONString(response.getValue(), SerializerFeature.WriteEnumUsingToString);
		logger.debug(result);
		Writer writer = new OutputStreamWriter(os, Charsets.UTF_8);
		writer.write(result);
		writer.close();
	}

	@Override
	protected final Request decodeRequest(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
		while (readSession(reader)) {
		}
		String serviceId = reader.readLine();
		String methodName = reader.readLine();

		Method method = serviceInvoker.getMethod(serviceId, methodName);
		Type[] types = method.getGenericParameterTypes();

		Request request = new Request();
		request.setService(serviceId);
		request.setMethod(methodName);
		if (types.length > 0) {
			String paramStr = reader.readLine();
			DefaultJSONParser parser = new DefaultJSONParser(paramStr, ParserConfig.getGlobalInstance());
			request.setArgs(parser.parseArray(types));
			logger.debug("{}|{}:{}", serviceId, methodName, paramStr);
		} else {
			request.setArgs(Utils.NULL_ARRAY);
		}
		return request;
	}

	private boolean readSession(BufferedReader reader) throws IOException {
		String key = reader.readLine();
		if (Strings.isNullOrEmpty(key))
			return false;
		String value = reader.readLine();
		Session.set(key, value);
		return true;
	}

}
