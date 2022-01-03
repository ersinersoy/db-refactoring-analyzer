package com.dbdependency.analyzer.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.dbdependency.analyzer.db.model.DBColumn;

public class DBColumnRowMapper implements RowMapper<DBColumn> {
    @Override
    public DBColumn mapRow(ResultSet rs, int rowNum) throws SQLException {
    	DBColumn dBColumn = new DBColumn();

    	dBColumn.setName(rs.getString("COLUMN_NAME"));
    	dBColumn.setSchema(rs.getString("OWNER"));
    	dBColumn.setTableView(rs.getString("TABLE_NAME"));
        return dBColumn;
    }
}