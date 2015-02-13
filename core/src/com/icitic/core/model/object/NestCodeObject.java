package com.icitic.core.model.object;

/**
 * 有Code，Name属性的Nest Model 树形对象
 * 
 * @author lijinghui
 *
 */
public class NestCodeObject<I> extends NestObject<I> implements INameObject, ICodeObject {

	private static final long serialVersionUID = 1L;

    protected String name;
    
    protected String code;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
