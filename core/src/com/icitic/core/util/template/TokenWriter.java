package com.icitic.core.util.template;

import java.io.IOException;
import java.io.Writer;

public interface TokenWriter {

	/**
	 * 输出一个token内容
	 * 
	 * @param writer
	 * @param token
	 * @throws IOException
	 */
	void write(Writer writer, String token) throws IOException;

	/**
	 * 开始一个循环
	 * 
	 * @param flag
	 *            循环标识
	 * @param loop
	 *            循环次数
	 * @return true继续，false停止循环
	 */
	boolean loopStart(String flag, int loop);

	/**
	 * 循环中的token输出
	 * 
	 * @param flag
	 *            循环标识
	 * @param loop
	 *            循环次数
	 * @param writer
	 * @param token
	 * @throws IOException
	 */
	void writeLoop(Writer writer, String flag, int loop, String token) throws IOException;

}
