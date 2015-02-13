package com.icitic.core.util.template;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.icitic.core.util.Utils;

public class Parser {

	private String template;

	private String openFlag;

	private String closeFlag;

	public Parser(String template, String openFlag, String closeFlag) {
		this.template = template;
		this.openFlag = openFlag;
		this.closeFlag = closeFlag;
	}

	public List<Part> parse() {
		ImmutableList.Builder<Part> builder = ImmutableList.builder();
		int index = 0;
		while (index < template.length()) {
			int next = nextFlag(index, openFlag);
			if (next == -1) {
				builder.add(Part.newText(template.substring(index)));
				break;
			} else {
				int len = next - index;
				if (len > 0)
					builder.add(Part.newText(template.substring(index, next)));
				index = next + openFlag.length();
				next = nextFlag(index, closeFlag);
				checkState(next > 0, "no closeFlag after", index);
				checkState(next > index, "null token found", index);

				String token = template.substring(index, next);
				index = next + closeFlag.length();
				if (token.equals("loop")) {
					index = doLoopThings(index, Utils.NULL_STR, builder);
				} else if (token.startsWith("loop ")) {
					index = doLoopThings(index, token.substring(5), builder);
				} else {
					builder.add(Part.newToken(token));
				}
			}
		}
		return builder.build();
	}

	private void checkState(boolean state, String msg, int index) {
		if (!state)
			throw new IllegalArgumentException(String.format("%s [%s..]", msg, template.substring(0, index)));
	}

	private int doLoopThings(int index, String loopFlag, ImmutableList.Builder<Part> builder) {
		StringBuilder sb = new StringBuilder().append(openFlag).append("end");
		if (!loopFlag.isEmpty())
			sb.append(" ").append(loopFlag);
		sb.append(closeFlag);
		int end = nextFlag(index, sb);
		checkState(end > 0, "no [end] found after", index);
		String sub = template.substring(index, end);
		List<Part> subParts = new Parser(sub, openFlag, closeFlag).parse();
		builder.add(Part.newLoop(loopFlag, subParts));
		return end + sb.length();
	}

	private boolean match(int index, CharSequence flag) {
		boolean match = true;
		for (int i = 1; i < flag.length(); i++) {
			if ((index + i >= template.length()) || template.charAt(index + i) != flag.charAt(i)) {
				match = false;
				break;
			}
		}
		return match;
	}

	private int nextFlag(int index, CharSequence flag) {
		while (index < template.length()) {
			if (template.charAt(index) == flag.charAt(0)) {
				if (match(index, flag)) {
					return index;
				}
			}
			index++;
		}
		return -1;
	}
}
