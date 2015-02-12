package com.icitic.core.db.object;

import com.icitic.core.model.object.INameObject;

/**
 * 系统对象扩展点
 * 
 * @author lijinghui
 * 
 */
public interface ObjectType extends INameObject {
    
    boolean hasSubType();

    String getObjectName(Object key);
    
    String getObjectName(String subType, Object key);

}
