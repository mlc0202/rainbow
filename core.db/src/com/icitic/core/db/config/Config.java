package com.icitic.core.db.config;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.icitic.core.util.XmlBinder;

@XmlRootElement
public class Config {

    @XmlElement(name = "physic")
    private List<Physic> physics;

    @XmlElement(name = "logic")
    private List<Logic> logics;

    public List<Physic> getPhysics() {
        if (physics == null)
            physics = new LinkedList<Physic>();
        return physics;
    }

    public List<Logic> getLogics() {
        if (logics == null)
            logics = new LinkedList<Logic>();
        return logics;
    }

    public static XmlBinder<Config> getXmlBinder() {
        return new XmlBinder<Config>("com.icitic.core.db.config", Config.class.getClassLoader());
    }

}
