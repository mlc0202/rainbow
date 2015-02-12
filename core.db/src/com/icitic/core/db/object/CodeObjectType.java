package com.icitic.core.db.object;

import com.google.common.base.Function;
import com.icitic.core.model.object.CodeObject;

public class CodeObjectType<I, T extends CodeObject<?>> extends NameObjectType<I, T> {

    private Function<String, T> codeFunc;

    public CodeObjectType(String name, Function<I, T> func, Function<String, T> codeFunc) {
        super(name, func);
        this.codeFunc = codeFunc;
    }

    @Override
    public boolean hasSubType() {
        return true;
    }

    @Override
    public String getObjectName(String subType, Object key) {
        if ("CODE".equals(subType)) {
            String code = (String) key;
            T obj = codeFunc.apply(code);
            return (obj == null) ? code : obj.getName();
        } else
            return getObjectName(key);
    }

}
