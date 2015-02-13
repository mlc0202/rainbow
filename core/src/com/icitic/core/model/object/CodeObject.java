package com.icitic.core.model.object;

/**
 * 有 id code name 属性的对象基类
 * 
 * @author lijinghui
 *
 */
public class CodeObject<I> extends NameObject<I> implements ICodeObject {

	private static final long serialVersionUID = 1L;

	protected String code;
	
	@Override
	public String getCode() {
		return code;
	}

	@Override
	public void setCode(String code) {
		this.code = code;
	}

}
