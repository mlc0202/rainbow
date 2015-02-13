package com.icitic.core.util;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class DateTimeSerializer implements ObjectSerializer {

	public final static DateTimeSerializer instance = new DateTimeSerializer();

	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
		SerializeWriter out = serializer.getWriter();

		if (object == null) {
			out.writeNull();
			return;
		}

		if (out.isEnabled(SerializerFeature.WriteClassName)) {
			if (object.getClass() != fieldType) {
				out.write('{');
				out.writeFieldName(JSON.DEFAULT_TYPE_KEY);
				serializer.write(object.getClass().getName());
				out.writeFieldValue(',', "val", ((Date) object).getTime());
				out.write('}');
				return;
			}
		}
		out.writeString(object.toString());
	}

}
