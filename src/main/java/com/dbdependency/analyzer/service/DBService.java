package com.dbdependency.analyzer.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.dbdependency.analyzer.db.mapper.DBColumnRowMapper;
import com.dbdependency.analyzer.db.mapper.DBObjectRowMapper;
import com.dbdependency.analyzer.db.mapper.DBSQLResultRowMapper;
import com.dbdependency.analyzer.db.mapper.DBSchemaRowMapper;
import com.dbdependency.analyzer.db.mapper.DBSourceCodeRowMapper;
import com.dbdependency.analyzer.db.mapper.DBTableViewRowMapper;
import com.dbdependency.analyzer.db.model.DBColumn;
import com.dbdependency.analyzer.db.model.DBObject;
import com.dbdependency.analyzer.db.model.DBSQLResult;
import com.dbdependency.analyzer.db.model.DBSchema;
import com.dbdependency.analyzer.db.model.DBSourceCode;
import com.dbdependency.analyzer.db.model.DBTableView;
import com.dbdependency.analyzer.model.Procedure;




@Repository
public class DBService {

	
	@Autowired
	JdbcTemplate jdbctemplate;
	
	
    public List<DBObject> findProcs(String objectOwner) {
    	

    	String query = "SELECT OBJECT_NAME,OBJECT_TYPE,OWNER FROM DBA_OBJECTS WHERE OBJECT_TYPE IN ('PROCEDURE','FUNCTION') AND OWNER=? AND STATUS='VALID' ORDER BY OBJECT_NAME";
    	

        List<DBObject> listDBObject = jdbctemplate.query(
        		  query, new Object[] { objectOwner }, new DBObjectRowMapper());
        return listDBObject;
    }
    

    public List<DBSchema> findSchemas(String objectOwner) {
    	

    	String query = "select OWNER, COUNT (*) OBJECTCOUNT  from DBA_OBJECTS where OBJECT_TYPE IN ('PROCEDURE','FUNCTION') and STATUS='VALID' \r\n"
    			+ "AND ((? IS NOT NULL AND\r\n"
    			+ "                   OWNER = ?) or\r\n"
    			+ "                   ? IS NULL) \r\n"
    			+ "group by OWNER\r\n"
    			+ "order by OBJECTCOUNT desc";
    	

        List<DBSchema> listDBSchema = jdbctemplate.query(
        		  query, new Object[] { objectOwner,objectOwner,objectOwner }, new DBSchemaRowMapper());
        return listDBSchema;
    }
    
    
    
    public List<DBTableView> findTableViewOfProc(String objectOwner,String objectName,String objectType) {
    	

    	String query = "SELECT REFERENCED_OWNER,REFERENCED_NAME,REFERENCED_TYPE,'dbaTableViewList.add(new DBATableView(\"'||REFERENCED_NAME||'\",\"'||REFERENCED_OWNER||'\",\"'||REFERENCED_TYPE||'\"));' DUMMMY FROM (\r\n"
    			+ "SELECT REFERENCED_OWNER,REFERENCED_NAME,REFERENCED_TYPE FROM DBA_DEPENDENCIES WHERE OWNER=? AND NAME=? AND TYPE=? AND REFERENCED_TYPE IN ('TABLE','VIEW') AND REFERENCED_NAME!='DUAL'\r\n"
    			+ "UNION \r\n"
    			+ "SELECT O.OWNER REFERENCED_OWNER,D.REFERENCED_NAME,O.OBJECT_TYPE REFERENCED_TYPE FROM DBA_DEPENDENCIES D,DBA_SYNONYMS S, DBA_OBJECTS O WHERE D.REFERENCED_OWNER=S.OWNER AND D.REFERENCED_NAME = S.SYNONYM_NAME\r\n"
    			+ "AND S.TABLE_OWNER = O.OWNER AND S.TABLE_NAME = O.OBJECT_NAME AND O.OBJECT_TYPE IN ('TABLE','VIEW')\r\n"
    			+ "AND D.OWNER=? AND D.NAME=? AND D.TYPE=? AND D.REFERENCED_TYPE='SYNONYM' AND D.REFERENCED_NAME!='DUAL')\r\n";
    	

        List<DBTableView> listDBTableView = jdbctemplate.query(
        		  query, new Object[] { objectOwner,objectName,objectType,objectOwner,objectName,objectType }, new DBTableViewRowMapper());
        return listDBTableView;
    }


    
    public List<DBTableView> findAllTableViewsWithName(String objectName) {
    	

    	String query = "SELECT OWNER REFERENCED_OWNER,OBJECT_NAME REFERENCED_NAME,OBJECT_TYPE REFERENCED_TYPE FROM DBA_OBJECTS WHERE OBJECT_NAME=? AND OBJECT_TYPE IN ('TABLE','VIEW')";
    	

        List<DBTableView> listDBTableView = jdbctemplate.query(
        		  query, new Object[] { objectName }, new DBTableViewRowMapper());
        return listDBTableView;
    }
    
    
    public List<DBColumn> findColumnsOfProc(String objectOwner,String objectName,String objectType) {
    	

    	String query = "SELECT OWNER,COLUMN_NAME,TABLE_NAME,'dbaColumnList.add(new DBAColumn(\"'||COLUMN_NAME||'\",\"'||TABLE_NAME||'\",\"'||OWNER||'\"));' DUMMMY \r\n"
    			+ "FROM DBA_TAB_COLS A,\r\n"
    			+ "(\r\n"
    			+ "SELECT REFERENCED_OWNER,REFERENCED_NAME,REFERENCED_TYPE FROM DBA_DEPENDENCIES WHERE OWNER=? AND NAME=? AND TYPE=? AND REFERENCED_TYPE IN ('TABLE','VIEW') AND REFERENCED_NAME!='DUAL'\r\n"
    			+ "UNION \r\n"
    			+ "SELECT O.OWNER REFERENCED_OWNER,D.REFERENCED_NAME,O.OBJECT_TYPE REFERENCED_TYPE FROM DBA_DEPENDENCIES D,DBA_SYNONYMS S, DBA_OBJECTS O WHERE D.REFERENCED_OWNER=S.OWNER AND D.REFERENCED_NAME = S.SYNONYM_NAME\r\n"
    			+ "AND S.TABLE_OWNER = O.OWNER AND S.TABLE_NAME = O.OBJECT_NAME AND O.OBJECT_TYPE IN ('TABLE','VIEW')\r\n"
    			+ "AND D.OWNER=? AND D.NAME=? AND D.TYPE=? AND D.REFERENCED_TYPE='SYNONYM' AND D.REFERENCED_NAME!='DUAL'\r\n"
    			+ ") B\r\n"
    			+ "WHERE A.OWNER=B.REFERENCED_OWNER AND A.TABLE_NAME=B.REFERENCED_NAME\r\n"
    			+ "ORDER BY COLUMN_NAME,TABLE_NAME";
    	

        List<DBColumn> listDBColumn = jdbctemplate.query(
        		  query, new Object[] { objectOwner,objectName,objectType,objectOwner,objectName,objectType }, new DBColumnRowMapper());
        return listDBColumn;
    }
    
    
    public List<DBColumn> findColumnsWithNameAndOwner(String objectOwner,String objectName) {
    	

    	String query = "SELECT OWNER,COLUMN_NAME,TABLE_NAME,'dbaColumnList.add(new DBAColumn(\"'||COLUMN_NAME||'\",\"'||TABLE_NAME||'\",\"'||OWNER||'\"));' DUMMMY FROM DBA_TAB_COLS WHERE OWNER=? AND TABLE_NAME=?";
    	

        List<DBColumn> listDBColumn = jdbctemplate.query(
        		  query, new Object[] { objectOwner,objectName }, new DBColumnRowMapper());
        return listDBColumn;
    }


    public List<DBSQLResult> getSQLResultList(String query) {
    	


        List<DBSQLResult> listDBSQLResult = jdbctemplate.query(
        		  query, new Object[] { }, new DBSQLResultRowMapper());
        return listDBSQLResult;
    }
    
    
    public DBSourceCode getSourceCode(String objectOwner,String objectName,String object_type) {
    	
    	String version="11.2";

    	String query = "SELECT DBMS_METADATA.GET_DDL('"+object_type+"','"+objectName+"','"+objectOwner+"','11.2') SOURCE_CODE FROM DUAL";


    	DBSourceCode dBASourceCode= jdbctemplate.queryForObject(
        		  query, new Object[] { }, new DBSourceCodeRowMapper());
        return dBASourceCode;
    }

    public DBSQLResult getSQLResult(String query) {
    	
    	DBSQLResult dBSQLResult= jdbctemplate.queryForObject(
        		  query, new Object[] { }, new DBSQLResultRowMapper());
        return dBSQLResult;
    }
    

    public void insertProcedureResult(int runId,Procedure aProcedure) {
    

    	 jdbctemplate.batchUpdate(
    	            "INSERT into DEPENDENCY_RESULT values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
    	            new BatchPreparedStatementSetter() {

    	                public void setValues(PreparedStatement ps, int i) throws SQLException {
    	                	ps.setInt(1, runId);
    	                	ps.setString(2, aProcedure.getName());
    	                    ps.setString(3, aProcedure.getSchema().getName());
    	                    ps.setString(4, aProcedure.getADependencyResultList().get(i).getOperation());
    	                    ps.setInt(5, aProcedure.getADependencyResultList().get(i).getLine());
    	                    ps.setString(6, aProcedure.getADependencyResultList().get(i).getColumn());
    	                    ps.setString(7, aProcedure.getADependencyResultList().get(i).getOwner());
    	                    ps.setString(8, aProcedure.getADependencyResultList().get(i).getTable());
    	                    if (aProcedure.getADependencyResultList().get(i).getStatement().getPureStatement().length()<4000)
    	                    {
        	                    ps.setString(9, aProcedure.getADependencyResultList().get(i).getStatement().getPureStatement());
    	                    }
    	                    else
    	                    {
        	                    ps.setString(9, aProcedure.getADependencyResultList().get(i).getStatement().getPureStatement().substring(0,3999));
    	                    	
    	                    }
    	                    
    	                	 int multibleAssignmentExist =0;
    	                	 int complexAssignmentExist =0;
    	                	 int stringConcatenationExist = 0;
    	                	 int tableNotCatched = 0;
    	                	 int isException = 0;
    	                	 
    	                	 if (aProcedure.getADependencyResultList().get(i).isException())
    	                	 {
    	                		 isException=1;
    	                	 }

    	                	 if (aProcedure.getADependencyResultList().get(i).isMultibleAssignmentExist())
    	                	 {
    	                		 multibleAssignmentExist=1;
    	                	 }
    	                	 
    	                	 if (aProcedure.getADependencyResultList().get(i).isComplexAssignmentExist())
    	                	 {
    	                		 complexAssignmentExist=1;
    	                	 }
    	                	 
    	                	 if (aProcedure.getADependencyResultList().get(i).isStringConcatenationExist())
    	                	 {
    	                		 stringConcatenationExist=1;
    	                	 }
    	                	 if (aProcedure.getADependencyResultList().get(i).isTableNotCatched())
    	                	 {
    	                		 tableNotCatched=1;
    	                	 }
    	                	 
    	                	 
    	                	 ps.setInt(10, isException);
    	                	 ps.setInt(11, multibleAssignmentExist);
    	                	 ps.setInt(12, complexAssignmentExist);
    	                	 ps.setInt(13, stringConcatenationExist);
    	                	 ps.setInt(14, tableNotCatched);

    	                }

    	                public int getBatchSize() {
    	                    return aProcedure.getADependencyResultList().size();
    	                }

    	            });    	
    	
    	
    	
    	
    }

    public void insertRunInformation(int runId,String runType,String owner,String objectName) {
        
   	        
    	
    	String insertSql = "INSERT INTO DEPENDENCY_RESULT_MAIN(NRUNID,TRUNTYPE,OWNER,OBJECT_NAME,DSTARTDATE) VALUES (?,?,?,?,?)";
   		// define query arguments
   	        Object[] params = new Object[] { runId, runType, owner,objectName, new Date() };
   	         
   	        // define SQL types of the arguments
   	        int[] types = new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP };
   	 
   	        // execute insert query to insert the data
   	        // return number of row / rows processed by the executed query
   	        jdbctemplate.update(insertSql, params, types);
   	
   }    
    

    public void insertSchemaInformation(int runId,String owner,int objectCount) {
        
   	        
    	
    	String insertSql = "INSERT INTO DEPENDENCY_RESULT_SCHEMA(NRUNID,OWNER,NOBJECTCOUNT,DSTARTDATE) values (?,?,?,?)";
   		// define query arguments
   	        Object[] params = new Object[] { runId, owner, objectCount, new Date() };
   	         
   	        // define SQL types of the arguments
   	        int[] types = new int[] { Types.NUMERIC, Types.VARCHAR, Types.NUMERIC, Types.TIMESTAMP };
   	 
   	        // execute insert query to insert the data
   	        // return number of row / rows processed by the executed query
   	        jdbctemplate.update(insertSql, params, types);
   	
   }    
    
    public void insertErrors(int runId,String owner,String objectName,String exceptionType, String error) {
        
   	        
    	
    	String insertSql = "INSERT INTO DEPENDENCY_RESULT_EXCEPTION(NRUNID,EXCEPTION_TYPE,OWNER,OBJECT_NAME,ERR) values (?,?,?,?,?)";
   		// define query arguments
   	        Object[] params = new Object[] { runId,exceptionType, owner, objectName,error };
   	         
   	        // define SQL types of the arguments
   	        int[] types = new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR };
   	 
   	        // execute insert query to insert the data
   	        // return number of row / rows processed by the executed query
   	        jdbctemplate.update(insertSql, params, types);
   	
   }    

    public void updateRunInformation(int runId) {
        
   	        
    	
    	String updateSql = "UPDATE DEPENDENCY_RESULT_MAIN SET DENDDATE=? WHERE NRUNID = ?";
   		// define query arguments
   	        Object[] params = new Object[] { new Date(),runId};
   	         
   	        // define SQL types of the arguments
   	        int[] types = new int[] { Types.TIMESTAMP,Types.NUMERIC };
   	 
   	        jdbctemplate.update(updateSql, params, types);
   	
   }    
    
    public void updateSchemaInformation(int runId) {
        
   	        
    	
    	String updateSql = "UPDATE DEPENDENCY_RESULT_SCHEMA SET DENDDATE=? WHERE NRUNID = ?";
   		// define query arguments
   	        Object[] params = new Object[] { new Date(),runId};
   	         
   	        // define SQL types of the arguments
   	        int[] types = new int[] { Types.TIMESTAMP,Types.NUMERIC };
   	 
   	        jdbctemplate.update(updateSql, params, types);
   	
   }    

    
    
}
