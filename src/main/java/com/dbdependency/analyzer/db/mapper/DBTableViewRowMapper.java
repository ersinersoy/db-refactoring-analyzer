package com.dbdependency.analyzer.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.dbdependency.analyzer.db.model.DBTableView;

public class DBTableViewRowMapper implements RowMapper<DBTableView> {
    @Override
    public DBTableView mapRow(ResultSet rs, int rowNum) throws SQLException {
    	DBTableView dBTableView = new DBTableView();

    	dBTableView.setName(rs.getString("REFERENCED_NAME"));
    	dBTableView.setSchema(rs.getString("REFERENCED_OWNER"));
    	dBTableView.setType(rs.getString("REFERENCED_TYPE"));
        return dBTableView;
    }
}