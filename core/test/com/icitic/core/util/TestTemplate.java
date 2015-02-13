package com.icitic.core.util;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.Writer;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.icitic.core.util.template.Template;
import com.icitic.core.util.template.TokenWriter;

public class TestTemplate {

	@Test
	public void testNormal() throws IOException {
		Function<String, String> fun = Functions.identity();
		Template t = new Template("abc @a@ @123@");
		String r = t.process(fun);
		assertEquals("abc a 123", r);

		try {
			t = new Template("@@");
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			t = new Template("@aaa@@");
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testLongFlag() throws IOException {
		Function<String, String> fun = Functions.identity();
		Template t = new Template("abc @@a@@ @@123@@", "@@");
		String r = t.process(fun);
		assertEquals("abc a 123", r);

		try {
			t = new Template("@@@@", "@@");
			fail();
		} catch (IllegalArgumentException e) {
		}

		try {
			t = new Template("@@aaa@", "@@");
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testWhile() throws IOException {
		TokenWriter tw = new TokenWriter() {

			@Override
			public boolean loopStart(String flag, int loop) {
				if (flag.equals("sub"))
					return loop < 2;
				return loop < 3;
			}

			@Override
			public void writeLoop(Writer writer, String flag, int loop, String token) throws IOException {
				if (token.equals("flag"))
					token = flag;
				writer.write(token + loop);
			}

			@Override
			public void write(Writer writer, String token) throws IOException {
				writer.write(token);
			}
		};
		Template t = new Template("[loop ok]abc[inx]-[ok]-[end ok]", "[", "]");
		String r = t.process(tw);
		assertEquals("abc1-ok1-abc2-ok2-", r);

		t = new Template("@loop ok@abc@inx@-@ok@-[@loop sub@@flag@@end sub@] @end ok@");
		r = t.process(tw);
		assertEquals("abc1-ok1-[sub1] abc2-ok2-[sub1] ", r);
	}

}
