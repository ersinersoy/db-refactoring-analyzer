package com.dbdependency.analyzer.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.dbdependency.analyzer.db.model.DBSchema;

public class DBSchemaRowMapper implements RowMapper<DBSchema> {
    @Override
    public DBSchema mapRow(ResultSet rs, int rowNum) throws SQLException {
    	DBSchema dBSchema = new DBSchema();

    	dBSchema.setOWNER(rs.getString("OWNER"));
    	dBSchema.setOBJECTCOUNT(rs.getInt("OBJECTCOUNT"));
        return dBSchema;
    }
}