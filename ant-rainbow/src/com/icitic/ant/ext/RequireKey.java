package com.icitic.ant.ext;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import com.google.common.base.Splitter;

public enum RequireKey {
	EXTEND("extend:") {
		protected void doRead(String content, int index, List<String> list) {
			readSingleClass(content, index, list);
		}
	},
	REQUIRES("requires:") {
		protected void doRead(String content, int index, List<String> list) {
			readSingleOrMultiClass(content, index, list);
		}
	},
	EXTREQUIRE("Ext.require(") {
		protected void doRead(String content, int index, List<String> list) {
			while (index >= 0) {
				index = readSingleOrMultiClass(content, index, list);
			}
		}
	},
	EXTSYNCREQUIRE("Ext.syncRequire(") {
		protected void doRead(String content, int index, List<String> list) {
			while (index >= 0) {
				index = readSingleOrMultiClass(content, index, list);
			}
		}
	},
	USES("uses:") {
		protected void doRead(String content, int index, List<String> list) {
			readMultiClass(content, index, list);
		}
	},
	MIXIN("mixins:") {
		protected void doRead(String content, int index, List<String> list) {
			if (content.charAt(index) == '{') {
				int endIndex = content.indexOf('}', ++index);
				checkState(endIndex > index, "no closing } found after %s{", key);
				String names = content.substring(index, endIndex).replace("\'", "");
				for (String name : Splitter.on(',').split(names)) {
					int item = name.indexOf(':');
					checkState(item > 0, "invalid mixin [%s]", name);
					list.add(name.substring(++item));
				}
			} else
				readMultiClass(content, index, list);
		}
	};

	protected String key;

	private RequireKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void readRequire(String content, List<String> list) {
		int index = content.indexOf(key);
		if (index == -1)
			return;
		index += key.length();
		doRead(content, index, list);
	}

	protected abstract void doRead(String content, int index, List<String> list);

	protected final int readSingleClass(String content, int index, List<String> list) {
		checkState(content.charAt(index) == '\'', "need \' after %s", key);
		int endIndex = content.indexOf('\'', ++index);
		checkState(endIndex > index, "no closing \' found after %s", key);
		list.add(content.substring(index, endIndex));
		index = content.indexOf(key, endIndex);
		return index>0 ? index + key.length() :index;
	}

	protected final int readMultiClass(String content, int index, List<String> list) {
		checkState(content.charAt(index) == '[', "need [ after %s", key);
		int endIndex = content.indexOf(']', ++index);
		checkState(endIndex > index, "no closing ] found after %s[", key);
		String names = content.substring(index, endIndex).replace("\'", "");
		for (String name : Splitter.on(',').split(names)) {
			list.add(name);
		}
		index = content.indexOf(key, endIndex);
		return index>0 ? index + key.length() :index;
	}

	protected final int readSingleOrMultiClass(String content, int index, List<String> list) {
		if (content.charAt(index) == '\'')
			return readSingleClass(content, index, list);
		else
			return readMultiClass(content, index, list);
	}

}
