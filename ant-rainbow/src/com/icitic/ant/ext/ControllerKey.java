package com.icitic.ant.ext;

import static com.google.common.base.Preconditions.*;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public enum ControllerKey {
    model("models:[") {
    },
    store("stores:[") {
    },
    view("views:[") {
    },
    controller("controllers:[") {
    },
    namespace("namespaces:[") {
    };

    protected String key;

    private ControllerKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public List<String> read(String content) {
        int index = content.indexOf(key);
        if (index == -1)
            return ImmutableList.of();
        index += key.length();
        int endIndex = content.indexOf(']', index);
        checkArgument(endIndex > index, "invalide %s config", key);
        String names = content.substring(index, endIndex).replace("\'", "");
        List<String> result = new LinkedList<String>();
        for (String name : Splitter.on(',').split(names)) {
            index = name.indexOf('@');
            if (index > 0) {
                name = new StringBuilder(name.substring(index + 1)).append('.').append(name.substring(0, index))
                        .toString();
            }
            result.add(name);
        }
        return result;
    }

    public String getFullName(String name, String ns) {
        if (name.contains(".")) {
            if (CharMatcher.JAVA_UPPER_CASE.apply(name.charAt(0))) {
                return name;
            }
        }
        return new StringBuilder(ns).append('.').append(name()).append('.').append(name).toString();
    }
}