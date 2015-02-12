package com.icitic.binding.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.icitic.core.service.Request;
import com.icitic.core.service.Response;
import com.icitic.core.service.ServiceInvoker;

public class TestJsonBinding {

	@Rule
	public static JUnitRuleMockery context = new JUnitRuleMockery();

	private static ServiceInvoker serviceInvoker;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		SerializeConfig.getGlobalInstance().setAsmEnable(false);
		ParserConfig.getGlobalInstance().setAsmEnable(false);

		serviceInvoker = context.mock(ServiceInvoker.class);
		final Method method = TestJsonBinding.class.getMethod("testMethod", Integer.TYPE, List.class);

		context.checking(new Expectations() {
			{
				allowing(serviceInvoker).getMethod("testService", "testMethod");
				will(returnValue(method));
				allowing(serviceInvoker).invoke(with(any(Request.class)));
				will(returnValue(new Response("12345")));
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() throws Exception {
		Calendar c = Calendar.getInstance();
		c.set(2000, 1, 1);
		long now = System.currentTimeMillis();
		List<Child> children = Lists.newArrayList(new Child("小宝", c.getTime()), new Child("阿珂", new Date(now)));
		Object[] params = new Object[] { 1, children };
		String paramStr = JSON.toJSONString(params);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, Charsets.UTF_8));
		writer.write('\r');
		writer.write("testService");
		writer.write('\r');
		writer.write("testMethod");
		writer.write('\r');
		writer.write(paramStr);
		writer.write('\r');
		writer.close();

		JsonBinding binding = new JsonBinding();
		binding.setServiceInvoker(serviceInvoker);
		InputStream is = new ByteArrayInputStream(out.toByteArray());

		Request request = binding.decodeRequest(is);
		assertEquals("testService", request.getService());
		assertEquals("testMethod", request.getMethod());
		assertEquals(2, request.getArgs().length);
		assertEquals(1, request.getArgs()[0]);
		Object param = request.getArgs()[1];
		assertTrue(param instanceof List);

		children = (List<Child>) param;
		Child child = children.get(0);
		assertEquals("小宝", child.getName());
		assertEquals(c.getTime(), child.getBirth());
		child = children.get(1);
		assertEquals("阿珂", child.getName());
		assertEquals(now, child.getBirth().getTime());
	}

	public void testMethod(int nouse, List<Child> children) {
	}
}
