package com.icitic.core.console;


/**
 * Console输出信息对象接口
 * 
 * @author lijinghui
 *
 */
public interface CommandInterpreter {

	/**
	 *	Get the next argument in the input.
	 *	
	 *	E.g. if the commandline is hello world, the _hello method
	 *	will get "world" as the first argument.
	 */
	public String nextArgument();

	/**
	 * Prints an object to the outputstream
	 *
	 * @param o	the object to be printed
	 */
	public void print(String msg, Object... args);

	/**
	 * Prints an empty line to the outputstream
	 */
	public void println();

	/**
	 * Prints an object to the output medium (appended with newline character).
	 * <p>
	 * If running on the target environment the user is prompted with '--more'
	 * if more than the configured number of lines have been printed without user prompt.
	 * That way the user of the program has control over the scrolling.
	 * <p>
	 * For this to work properly you should not embedded "\n" etc. into the string.
	 *
	 * @param	o	the object to be printed
	 */
	public void println(String msg, Object... args);

	/**
	 * Print a stack trace including nested exceptions.
	 * @param t The offending exception
	 */
	public void printStackTrace(Throwable t);

}
