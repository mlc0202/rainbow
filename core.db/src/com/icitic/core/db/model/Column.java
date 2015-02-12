package com.icitic.core.db.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.icitic.core.model.object.INameObject;

@XmlType(name = "Column", propOrder = { "name", "dbName", "cnName", "type", "javaType", "length", "precision", "key",
        "mandatory" })
public class Column implements INameObject{

    @XmlElement(required = true)
    private String name;

    @XmlElement(required = true)
    private String dbName;

    @XmlElement(required = true)
    private String cnName;

    @XmlElement(required = true)
    private ColumnType type;

    @XmlElement(required = true)
    private JavaType javaType;

    private int length;

    private int precision;

    private boolean key;

    private boolean mandatory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public JavaType getJavaType() {
        return javaType;
    }

    public void setJavaType(JavaType javaType) {
        this.javaType = javaType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Object fromDb(Object value) {
        return fromDb(javaType, value);
    }

    public Object toDb(Object value) {
        return toDb(type, value);
    }

    /**
     * 翻译从数据库中得到的数据为Java类型
     * 
     * @param value
     * @return
     */
    public static Object fromDb(JavaType javaType, Object value) {
        if (value == null)
            return null;
        switch (javaType) {
        case INT:
            checkArgument(value instanceof Number);
            return ((Number) value).intValue();
        case BOOL:
            if (value instanceof Number)
                return ((Number) value).intValue() == 1;
            if (value instanceof String)
                return "1".equals(value);
            break;
        case LONG:
            checkArgument(value instanceof Number);
            return ((Number) value).longValue();
        case DOUBLE:
            checkArgument(value instanceof Number);
            return ((Number) value).doubleValue();
        case BIGDECIMAL:
            if (value instanceof BigDecimal)
                return value;
            checkArgument(value instanceof Number);
            return new BigDecimal(((Number) value).toString());
        case STRING:
            if (value instanceof String)
                return value;
            if (value instanceof Clob)
                return getClob((Clob) value);
            break;
        case DATE:
            if (value instanceof Date)
                return value;
            if (value instanceof Number)
                return new Date(((Number) value).longValue());
            break;
        case BYTEARRAY:
            if (value instanceof byte[])
                return value;
            if (value instanceof Blob)
                return getBlob((Blob) value);
            break;
        }
        throw new IllegalArgumentException();
    }

    private static String getClob(Clob clob) {
        try {
            return CharStreams.toString(clob.getCharacterStream());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static byte[] getBlob(Blob blob) {
        try {
            return ByteStreams.toByteArray(blob.getBinaryStream());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * 翻译从Java类型的数据到数据库中
     * 
     * @param value
     * @return
     */
    public static Object toDb(ColumnType type, Object value) {
        if (value == null)
            return null;
        switch (type) {
        case SMALLINT:
        case INT:
            if (value instanceof Boolean)
                return ((Boolean) value) ? 1 : 0;
            if (value instanceof Enum)
                return ((Enum<?>) value).ordinal();
            break;
        case LONG:
            if (value instanceof Date)
                return ((Date) value).getTime();
            break;
        case CHAR:
        case VARCHAR:
            if (value instanceof Boolean)
                return ((Boolean) value) ? "1" : "0";
            if (value instanceof Enum)
                return ((Enum<?>) value).name();
            break;
        default:
        	break;
        }
        return value;
    }

}
