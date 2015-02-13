package com.icitic.core.service;


/**
 * 服务请求封装对象,请求的参数可以是数组，也可以是Map
 * 
 * @author lijinghui
 * 
 */
public class Request {

	/** 服务名 */
	private String service;

	/** 请求方法 */
	private String method;

	/** 请求参数 */
	private Object[] args;

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

}