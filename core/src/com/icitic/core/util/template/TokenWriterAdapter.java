package com.icitic.core.util.template;

import java.io.IOException;
import java.io.Writer;

public class TokenWriterAdapter implements TokenWriter {

	@Override
	public void write(Writer writer, String token) throws IOException {
		String value = get(token);
		if (value != null)
			writer.write(value);
	}

	@Override
	public boolean loopStart(String flag, int loop) {
		return false;
	}

	@Override
	public void writeLoop(Writer writer, String flag, int loop, String token) throws IOException {
		String value = loopGet(flag, loop, token);
		if (value != null)
			writer.write(value);
	}

	protected String get(String token) {
		return token;
	}
	
	protected String loopGet(String flag, int loop, String token) {
		return token;
	}
}
