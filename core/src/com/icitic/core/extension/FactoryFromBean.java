package com.icitic.core.extension;

import com.icitic.core.util.ioc.Context;

/**
 * @author lijinghui
 * 
 */
public class FactoryFromBean extends Factory {

	/**
	 * context
	 */
	private Context context;

	public FactoryFromBean(Context context, Class<?> clazz) {
		super(clazz);
		this.context = context;
	}

	@Override
	public Object createInstance() {
		return context.getBean(clazz);
	}

}
