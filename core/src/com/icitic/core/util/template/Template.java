package com.icitic.core.util.template;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Throwables;

public class Template {

	private static final Logger logger = LoggerFactory.getLogger(Template.class);

	private List<Part> parts;

	public Template(String template) {
		this(template, "@");
	}

	public Template(String template, String flag) {
		this(template, flag, flag);
	}

	public Template(String template, String openFlag, String closeFlag) {
		parts = new Parser(template, openFlag, closeFlag).parse();
	}

	public void process(Writer writer, TokenWriter tokenWriter) {
		try {
			export(writer, tokenWriter, parts, null, 0);
		} catch (IOException e) {
			logger.error("when process template", e);
			Throwables.propagate(e);
		}
	}

	private void export(Writer writer, TokenWriter tokenWriter, List<Part> parts, String loopFlag, int loopInx)
			throws IOException {
		for (Part part : parts) {
			if (part.isToken()) {
				String token = part.getContent();
				if ("inx".equals(token))
					writer.write(Integer.toString(loopInx));
				else if (loopFlag == null)
					tokenWriter.write(writer, token);
				else
					tokenWriter.writeLoop(writer, loopFlag, loopInx, token);
			} else if (part.isLoop()) {
				int inx = 1;
				while (tokenWriter.loopStart(part.getContent(), inx)) {
					export(writer, tokenWriter, part.getSub(), part.getContent(), inx++);
				}
			} else {
				writer.write(part.getContent());
			}
		}
	}

	public void process(Writer writer, final Function<String, String> tokenFun) {
		process(writer, new TokenWriterAdapter() {
			@Override
			protected String get(String token) {
				return tokenFun.apply(token);
			}
		});
	}

	public void process(Writer writer, final Map<String, String> tokenMap) {
		process(writer, new TokenWriterAdapter() {
			@Override
			protected String get(String token) {
				return tokenMap.get(token);
			}
		});
	}

	public String process(TokenWriter tokenWriter) {
		StringWriter writer = new StringWriter();
		process(writer, tokenWriter);
		return writer.toString();
	}

	public String process(final Function<String, String> tokenFun) {
		StringWriter writer = new StringWriter();
		process(writer, tokenFun);
		return writer.toString();
	}

	public String process(final Map<String, String> tokenMap) {
		StringWriter writer = new StringWriter();
		process(writer, tokenMap);
		return writer.toString();
	}

}
