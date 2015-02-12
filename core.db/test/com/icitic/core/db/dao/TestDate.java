package com.icitic.core.db.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.ColumnType;
import com.icitic.core.db.model.Entity;
import com.icitic.core.db.model.JavaType;

public class TestDate {
    private int id;
    private Date date1;
    private Date date2;
    private Date date3;
    private Date date4;
    private Date date5;
    private Date date6;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate1() {
        return date1;
    }

    public void setDate1(Date date1) {
        this.date1 = date1;
    }

    public Date getDate2() {
        return date2;
    }

    public void setDate2(Date date2) {
        this.date2 = date2;
    }

    public Date getDate3() {
        return date3;
    }

    public void setDate3(Date date3) {
        this.date3 = date3;
    }

    public Date getDate4() {
        return date4;
    }

    public void setDate4(Date date4) {
        this.date4 = date4;
    }

    public Date getDate5() {
        return date5;
    }

    public void setDate5(Date date5) {
        this.date5 = date5;
    }

    public Date getDate6() {
        return date6;
    }

    public void setDate6(Date date6) {
        this.date6 = date6;
    }

    public static Entity getEntity() {
        Entity entity = new Entity();
        entity.setCnName("日期测试实体");
        entity.setDbName("TBL_TEST_DATE");
        entity.setName("TestDate");
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
        column.setCnName("日期1");
        column.setDbName("date1");
        column.setName("date1");
        column.setJavaType(JavaType.DATE);
        column.setType(ColumnType.VARCHAR);
        column.setLength(17);
        columns.add(column);

        column = new Column();
        column.setCnName("日期2");
        column.setDbName("date2");
        column.setName("date2");
        column.setJavaType(JavaType.DATE);
        column.setType(ColumnType.LONG);
        columns.add(column);

        column = new Column();
        column.setCnName("日期3");
        column.setDbName("date3");
        column.setName("date3");
        column.setJavaType(JavaType.DATE);
        column.setType(ColumnType.DATE);
        columns.add(column);

        column = new Column();
        column.setCnName("日期4");
        column.setDbName("date4");
        column.setName("date4");
        column.setJavaType(JavaType.DATE);
        column.setType(ColumnType.TIME);
        columns.add(column);

        column = new Column();
        column.setCnName("日期5");
        column.setDbName("date5");
        column.setName("date5");
        column.setJavaType(JavaType.DATE);
        column.setType(ColumnType.TIMESTAMP);
        columns.add(column);

        column = new Column();
        column.setCnName("日期6");
        column.setDbName("date6");
        column.setName("date6");
        column.setJavaType(JavaType.DATE);
        column.setType(ColumnType.CHAR);
        column.setLength(17);
        columns.add(column);

        entity.afterLoad();
        return entity;
    }
}
