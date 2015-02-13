package com.icitic.core.service.channel;

import com.icitic.core.model.object.SimpleNameObject;

public abstract class ServerConfigParam extends SimpleNameObject {

    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public abstract Object accept(Object input) throws IllegalArgumentException;

}
