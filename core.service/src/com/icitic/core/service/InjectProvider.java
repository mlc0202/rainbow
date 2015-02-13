package com.icitic.core.service;

/**
 * 注入对象类型提供扩展点
 * 
 * @author lijinghui
 *
 * @param <T>
 */
public interface InjectProvider {

    /**
     * 返回是否提供指定的注入类型
     * 
     * @param injectType
     * @return
     */
    public boolean provide(Class<?> injectType);

    /**
     * 返回指定的类型与名字的注入对象
     * 
     * @param name
     * @return
     */
    public Object getInjectObject(Class<?> injectType, String name);

}
