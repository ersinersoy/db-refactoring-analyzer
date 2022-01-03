package com.dbdependency.analyzer.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.dbdependency.analyzer.db.model.DBSQLResult;

public class DBSQLResultRowMapper implements RowMapper<DBSQLResult> {
    @Override
    public DBSQLResult mapRow(ResultSet rs, int rowNum) throws SQLException {
    	DBSQLResult dBSQLResult = new DBSQLResult();

    	dBSQLResult.setResult(rs.getString("RESULT"));

        return dBSQLResult;
    }
}