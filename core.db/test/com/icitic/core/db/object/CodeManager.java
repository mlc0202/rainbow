package com.icitic.core.db.object;

import com.icitic.core.db.object.ObjectTypeAdapter;

public class CodeManager extends ObjectTypeAdapter {

    @Override
    public String getName() {
        return "CODE";
    }

    @Override
    public boolean hasSubType() {
        return true;
    }

    @Override
    public String getObjectName(Object key) {
        throw new IllegalArgumentException("code type not specified");
    }

    @Override
    public String getObjectName(String subType, Object key) {
        if ("01".equals(subType)) {
            if ("1".equals(key))
                return "男";
            else
                return "女";
        } else if ("02".equals(subType)) {
            if ("1".equals(key))
                return "职员";
            else
                return "老板";
        }
        throw new IllegalArgumentException("code type not valid");
    }

}
