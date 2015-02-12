package com.icitic.core.db.dao;

import java.util.ArrayList;
import java.util.List;

import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.ColumnType;
import com.icitic.core.db.model.Entity;
import com.icitic.core.db.model.JavaType;

public class TestXlob {
    private int id;
    private byte[] blob;
    private String clob;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getBlob() {
        return blob;
    }

    public void setBlob(byte[] blob) {
        this.blob = blob;
    }

    public String getClob() {
        return clob;
    }

    public void setClob(String clob) {
        this.clob = clob;
    }

    public static Entity getEntity() {
        Entity entity = new Entity();
        entity.setCnName("Xlob测试实体");
        entity.setDbName("TBL_TEST_XLOB");
        entity.setName("TestXlob");
        List<Column> columns = new ArrayList<Column>();
        entity.setColumns(columns);

        Column column = new Column();
        column.setCnName("编号");
        column.setDbName("ID");
        column.setName("id");
        column.setJavaType(JavaType.INT);
        column.setType(ColumnType.INT);
        column.setKey(true);
        columns.add(column);

        column = new Column();
        column.setCnName("Blob字段");
        column.setDbName("blobCol");
        column.setName("blob");
        column.setJavaType(JavaType.BYTEARRAY);
        column.setType(ColumnType.BLOB);
        columns.add(column);

        column = new Column();
        column.setCnName("Clob字段");
        column.setDbName("clobCol");
        column.setName("clob");
        column.setJavaType(JavaType.STRING);
        column.setType(ColumnType.CLOB);
        columns.add(column);

        entity.afterLoad();
        return entity;
    }
}
