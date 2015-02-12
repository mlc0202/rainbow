package com.icitic.core.db.object;

import com.icitic.core.db.object.ObjectTypeAdapter;


public class DeviceManager extends ObjectTypeAdapter {

    public static final String TYPE_NAME = "device";

    @Override
    public String getName() {
        return TYPE_NAME;
    }

    @Override
    public String getObjectName(Object key) {
        int id = (Integer) key;
        switch (id) {
        case 1:
            return "iPod";
        case 2:
            return "iPhone";
        case 3:
            return "iPad";
        default:
            return Integer.toString(id);
        }
    }

}
