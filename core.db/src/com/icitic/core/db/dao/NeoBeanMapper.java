package com.icitic.core.db.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.icitic.core.db.jdbc.JdbcUtils;
import com.icitic.core.db.jdbc.RowMapper;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.Entity;

public class NeoBeanMapper implements RowMapper<NeoBean> {

    private Entity entity;

    public NeoBeanMapper(Entity entity) {
        this.entity = entity;
    }

    @Override
    public NeoBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        NeoBean bean = new NeoBean(entity);
        Map<String, Integer> nameIndexMap = getColNameIndexMapper(rs);
        for (Column column : entity.getColumns()) {
            Integer index = nameIndexMap.get(column.getDbName().toUpperCase());
            if (index != null) {
                Object obj = JdbcUtils.getResultSetValue(rs, index);
                obj = column.fromDb(obj);
                try {
                    bean.setObject(column, obj);
                } catch (IllegalArgumentException e) {
                }
            }
        }
        return bean;
    }

    private Map<String, Integer> getColNameIndexMapper(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        Map<String, Integer> nameIndexMap = new HashMap<String, Integer>();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            String colName = md.getColumnLabel(i);
            if (colName == null || colName.length() < 1) {
                colName = md.getColumnName(i);
            }

            nameIndexMap.put(colName.toUpperCase(), i);
        }
        return nameIndexMap;
    }
}
