package com.icitic.core.db.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.icitic.core.util.XmlBinder;

@XmlRootElement
@XmlType(name = "")
public class Model {

    private String name;

    @XmlElementWrapper(name = "entities", required = true)
    @XmlElement(name = "entity", required = true)
    private List<Entity> entities;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Entity> getEntities() {
        if (entities == null)
            entities = new ArrayList<Entity>();
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public static XmlBinder<Model> getXmlBinder() {
        return new XmlBinder<Model>("com.icitic.core.db.model", Model.class.getClassLoader());
    }
}
