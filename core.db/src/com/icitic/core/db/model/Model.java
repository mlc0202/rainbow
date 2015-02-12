package com.icitic.core.db.model;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.ImmutableMap;
import com.icitic.core.util.XmlBinder;

@XmlRootElement
@XmlType(name = "")
public class Model {

    private String name;

    @XmlElementWrapper(name = "entities", required = true)
    @XmlElement(name = "entity", required = true)
    private List<Entity> entities;

    @XmlTransient
    private Map<String, Entity> entityMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public Entity getEntity(String entityName) {
        return entityMap.get(entityName);
    }

    /**
     * 从XML导入后必须调用此函数以初始化map
     */
    public void afterLoad() {
        ImmutableMap.Builder<String, Entity> builder = ImmutableMap.builder();
        for (Entity entity : entities) {
            builder.put(entity.getName(), entity);
            entity.afterLoad();
        }
        entityMap = builder.build();
    }

    public Map<String, Entity> getEntityMap() {
        return ImmutableMap.copyOf(entityMap);
    }

    public static XmlBinder<Model> getXmlBinder() {
        return new XmlBinder<Model>("com.icitic.core.db.model", Model.class.getClassLoader());
    }
}
