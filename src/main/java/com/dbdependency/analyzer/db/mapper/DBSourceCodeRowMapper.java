package com.dbdependency.analyzer.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.dbdependency.analyzer.db.model.DBSourceCode;

public class DBSourceCodeRowMapper implements RowMapper<DBSourceCode> {
    @Override
    public DBSourceCode mapRow(ResultSet rs, int rowNum) throws SQLException {
    	DBSourceCode dBASourceCode = new DBSourceCode();

    	dBASourceCode.setSourcecode(rs.getString("SOURCE_CODE"));

        return dBASourceCode;
    }
}