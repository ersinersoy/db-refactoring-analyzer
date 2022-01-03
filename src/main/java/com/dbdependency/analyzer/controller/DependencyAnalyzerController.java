package com.dbdependency.analyzer.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dbdependency.analyzer.db.model.DBColumn;
import com.dbdependency.analyzer.db.model.DBObject;
import com.dbdependency.analyzer.db.model.DBSQLResult;
import com.dbdependency.analyzer.db.model.DBSchema;
import com.dbdependency.analyzer.db.model.DBSourceCode;
import com.dbdependency.analyzer.db.model.DBTableView;
import com.dbdependency.analyzer.model.Assignment;
import com.dbdependency.analyzer.model.Column;
import com.dbdependency.analyzer.model.Procedure;
import com.dbdependency.analyzer.model.Schema;
import com.dbdependency.analyzer.model.Statement;
import com.dbdependency.analyzer.model.TableView;
import com.dbdependency.analyzer.model.Variable;
import com.dbdependency.analyzer.model.result.DependencyResult;
import com.dbdependency.analyzer.parser.core.DependencyAnalyzer;
import com.dbdependency.analyzer.service.DBService;

@Component
public class DependencyAnalyzerController {

	@Autowired
	DBService aDBService;

	public DBSourceCode getSourceCode(String objectOwner, String objectName, String object_type) {

		DBSourceCode res = aDBService.getSourceCode(objectOwner, objectName, object_type);
		return res;
	}

	public List<DBObject> findProcs(String objectOwner) {

		List<DBObject> res = aDBService.findProcs(objectOwner);
		return res;
	}

	public List<DBTableView> findTableViewOfProc(String objectOwner, String objectName, String objectType) {

		List<DBTableView> res = aDBService.findTableViewOfProc(objectOwner, objectName, objectType);
		return res;
	}

	public List<DBColumn> findColumnsOfProc(String objectOwner, String objectName, String objectType) {

		List<DBColumn> res = aDBService.findColumnsOfProc(objectOwner, objectName, objectType);
		return res;

	}

	public List<DBTableView> findAllTableViewsWithName(String objectName) {

		List<DBTableView> res = aDBService.findAllTableViewsWithName(objectName);
		return res;

	}

	public List<DBColumn> findColumnsWithNameAndOwner(String objectOwner, String objectName) {

		List<DBColumn> res = aDBService.findColumnsWithNameAndOwner(objectOwner, objectName);
		return res;

	}

	public Map<String, Column> deneme(List<DBColumn> dbaColumnList) {
		Map<String, Column> a = DependencyAnalyzer.fillColumnMap(dbaColumnList);
		return a;
	}

	public Procedure analyzeDependency(DBObject aDBObject) throws Exception {

		DBSourceCode aDBSourceCode = getSourceCode(aDBObject.getOWNER(), aDBObject.getOBJECT_NAME(),
				aDBObject.getOBJECT_TYPE());

		List<Statement> allStatements = DependencyAnalyzer.getParserResults(aDBSourceCode.getSourcecode());

		// it is used to hold dependent schemas
		Map<String, Schema> spSchema = new LinkedHashMap<String, Schema>();

		// Collect dependent tables of procedure from database
		List<DBTableView> dbaTableViewList = findTableViewOfProc(aDBObject.getOWNER(), aDBObject.getOBJECT_NAME(),
				aDBObject.getOBJECT_TYPE());
		Map<String, TableView> spTableView = DependencyAnalyzer.fillTableViewMap(spSchema, dbaTableViewList);

		// Collect dependent tables columns of procedure from database
		List<DBColumn> dbaColumnList = findColumnsOfProc(aDBObject.getOWNER(), aDBObject.getOBJECT_NAME(),
				aDBObject.getOBJECT_TYPE());
		Map<String, Column> spTableViewColumns = DependencyAnalyzer.fillColumnMap(dbaColumnList);

		Schema aSchema = new Schema();
		aSchema.setName(aDBObject.getOWNER());
		Procedure aProcedure = new Procedure();
		aProcedure.setName(aDBObject.getOBJECT_NAME());
		aProcedure.setSchema(aSchema);

		Map<String, Procedure> procedureMap = new LinkedHashMap<String, Procedure>();
		procedureMap.put(null, aProcedure);

		aSchema.setProcedureMap(procedureMap);

		// it is used to hold all variables
		Map<String, Variable> avariableMap = new LinkedHashMap<String, Variable>();

		// it is used to hold all variable assignments
		List<Assignment> aAssignmentList = null;

		List<DependencyResult> aDependencyResultList = new ArrayList<DependencyResult>();
		aProcedure.setADependencyResultList(aDependencyResultList);

		return DependencyAnalyzer.findPLSQLdependency(allStatements, spTableView, spTableViewColumns, aProcedure,
				avariableMap, aAssignmentList, aDependencyResultList, false);

	}

	public void extendTableViewColumnMap(String tableViewName, Map<String, Column> spTableViewColumns,
			Map<String, TableView> spTableView) throws Exception {

		List<DBTableView> dbaTableViewList = findAllTableViewsWithName(tableViewName);

		for (DBTableView dbaTableView : dbaTableViewList) {

			if (spTableView.containsKey(dbaTableView.getName())) {

			} else {
				Schema aTempSchema = new Schema(dbaTableView.getSchema(), null);
				Map<String, Schema> aTempSchemaMap = new LinkedHashMap<String, Schema>();
				aTempSchemaMap.put(dbaTableView.getSchema(), aTempSchema);
				TableView aTempTableView = new TableView(dbaTableView.getName(), 0, false, aTempSchemaMap,
						dbaTableView.getType());
				spTableView.put(dbaTableView.getName(), aTempTableView);

			}

			List<DBColumn> dbaColumnList = findColumnsWithNameAndOwner(dbaTableView.getSchema(),
					dbaTableView.getName());

			for (DBColumn dbaColumn : dbaColumnList) {

				if (spTableViewColumns.containsKey(dbaColumn.getName())) {

					Column aTempColumn = spTableViewColumns.get(dbaColumn.getName());
					Map<String, TableView> aTempTableViewMap = aTempColumn.getTableView();

					Schema aTempSchema = new Schema(dbaColumn.getSchema(), null);
					Map<String, Schema> aTempSchemaMap = new LinkedHashMap<String, Schema>();
					aTempSchemaMap.put(dbaColumn.getSchema(), aTempSchema);

					TableView aTempTableView = new TableView(dbaColumn.getTableView(), 0, false, aTempSchemaMap,
							"TABLEVIEW");
					aTempTableViewMap.put(dbaColumn.getTableView(), aTempTableView);

					aTempColumn.setMultibleTableViewExist(true);

				} else {

					Schema aTempSchema = new Schema(dbaColumn.getSchema(), null);
					Map<String, Schema> aTempSchemaMap = new LinkedHashMap<String, Schema>();
					aTempSchemaMap.put(dbaColumn.getSchema(), aTempSchema);

					TableView aTempTableView = new TableView(dbaColumn.getTableView(), 0, false, aTempSchemaMap,
							"TABLEVIEW");

					Map<String, TableView> aTempTableViewMap = new LinkedHashMap<String, TableView>();

					aTempTableViewMap.put(dbaColumn.getTableView(), aTempTableView);

					Column aTempColumn = new Column(dbaColumn.getName(), 0, false, aTempTableViewMap);
					spTableViewColumns.put(dbaColumn.getName(), aTempColumn);

				}

			}

		}

	}

	public void insertResult(int runId,Procedure aProcedure) {
		aDBService.insertProcedureResult(runId,aProcedure);
	}

	public List<DBSchema> findSchemas(String objectOwner) {
		return aDBService.findSchemas(objectOwner);
	}

	public DBSQLResult getSQLResult(String query) {
		return aDBService.getSQLResult(query);
	}

	public void insertRunInformation(int runId, String runType, String owner,String objectName) {
		aDBService.insertRunInformation(runId, runType, owner,objectName);
	}

	public void insertSchemaInformation(int runId, String owner, int objectCount) {
		aDBService.insertSchemaInformation(runId, owner, objectCount);
	}

	public void insertErrors(int runId, String owner, String objectName,String exceptionType, String error) {
		aDBService.insertErrors(runId, owner, objectName,exceptionType,error);
	}

	public void updateRunInformation(int runId) {
		aDBService.updateRunInformation(runId);
	}

	public void updateSchemaInformation(int runId) {
		aDBService.updateSchemaInformation(runId);
	}
	
	 public List<DBSQLResult> getSQLResultList(String query) {
		 return aDBService.getSQLResultList(query);
	 }
	
}
