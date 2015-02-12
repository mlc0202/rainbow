package com.icitic.core.db.dao.object;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.icitic.core.db.jdbc.RowMapper;
import com.icitic.core.model.object.CodeObject;

public class CodeObjectRowMapper implements RowMapper<CodeObject<Integer>> {

    @Override
    public CodeObject<Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
        CodeObject<Integer> obj = new CodeObject<Integer>();
        obj.setId(rs.getInt("id"));
        obj.setCode(rs.getString("code"));
        obj.setName(rs.getString("name"));
        return obj;
    }

}
