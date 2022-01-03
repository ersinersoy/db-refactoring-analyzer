package com.dbdependency.analyzer.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.dbdependency.analyzer.db.model.DBObject;

public class DBObjectRowMapper implements RowMapper<DBObject> {
    @Override
    public DBObject mapRow(ResultSet rs, int rowNum) throws SQLException {
    	DBObject dbaObject = new DBObject();

    	dbaObject.setOBJECT_NAME(rs.getString("OBJECT_NAME"));
    	dbaObject.setOWNER(rs.getString("OWNER"));
    	dbaObject.setOBJECT_TYPE(rs.getString("OBJECT_TYPE"));
        return dbaObject;
    }
}