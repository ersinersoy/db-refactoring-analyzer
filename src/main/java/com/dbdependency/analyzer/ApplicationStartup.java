package com.dbdependency.analyzer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.dbdependency.analyzer.controller.DependencyAnalyzerController;
import com.dbdependency.analyzer.db.model.DBObject;
import com.dbdependency.analyzer.db.model.DBSQLResult;
import com.dbdependency.analyzer.db.model.DBSchema;
import com.dbdependency.analyzer.model.ErrorInfo;
import com.dbdependency.analyzer.model.Procedure;
import com.dbdependency.analyzer.parser.util.DependencyAnalyzerUtil;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {


	@Autowired
	DependencyAnalyzerController dependencyAnalyzerController;

	@Value("${app.runType}")
	int runType;

	@Value("${app.schema}")
	String objectOwner;

	@Value("${app.singlerun.object}")
	String singleRunObject;

	@Value("${app.singlerun.object.type}")
	String singleRunObjectType;

	String nextMaxRunIdSql = "SELECT MAX(NRUNID)+1 RESULT from DEPENDENCY_RESULT_MAIN";

	@Override
	public void onApplicationEvent(final ApplicationReadyEvent event) {

		
		System.out.println("DEPENDENCY ANALYZER STARTED");

		DBSQLResult aSqlResult = dependencyAnalyzerController.getSQLResult(nextMaxRunIdSql);
		
		int nextRunId = 0;

		if (aSqlResult.getResult() == null) {
			nextRunId = 1; // First run
		} else {
			nextRunId = Integer.valueOf(aSqlResult.getResult()).intValue();
		}

		if (runType == 1) // Single Object Run
		{

			
			DBObject aDBObject = new DBObject();
			aDBObject.setOBJECT_NAME(singleRunObject);
			aDBObject.setOBJECT_TYPE(singleRunObjectType);
			aDBObject.setOWNER(objectOwner);

			try {

				dependencyAnalyzerController.insertRunInformation(nextRunId, "SINGLE", aDBObject.getOWNER(),
						aDBObject.getOBJECT_NAME());

				Procedure aProcedure = dependencyAnalyzerController.analyzeDependency(aDBObject);
				dependencyAnalyzerController.insertResult(nextRunId, aProcedure);

				if (aProcedure.getADependencyResultList().size() == 0) {
					dependencyAnalyzerController.insertErrors(nextRunId, aDBObject.getOWNER(),
							aDBObject.getOBJECT_NAME(), "NODEPENDENCY",null);
				}

				dependencyAnalyzerController.updateRunInformation(nextRunId);

				DependencyAnalyzerUtil.printSimpleDependencyResult(aProcedure);

			} catch (Exception e) {

				dependencyAnalyzerController.insertErrors(nextRunId, aDBObject.getOWNER(), aDBObject.getOBJECT_NAME(),
						"ERROR",e.toString().substring(0, e.toString().length()-1));
				e.printStackTrace();
			}

		} else if (runType == 2) { // Single Schema Run

			List<ErrorInfo> errorProcs = new ArrayList<ErrorInfo>();
			
			int errorCount = 0;

			
			
			dependencyAnalyzerController.insertRunInformation(nextRunId, "SCHEMA", objectOwner, null);

			List<DBSchema> dBSchemaList = dependencyAnalyzerController.findSchemas(objectOwner);

			dependencyAnalyzerController.insertSchemaInformation(nextRunId, objectOwner,
					dBSchemaList.get(0).getOBJECTCOUNT());

			List<DBObject> res = dependencyAnalyzerController.findProcs(objectOwner);
			for (DBObject aDBObject : res) {

				System.out.println("START OBJECT: " + aDBObject.getOBJECT_NAME());

				try {
					Procedure aProcedure = dependencyAnalyzerController.analyzeDependency(aDBObject);
					dependencyAnalyzerController.insertResult(nextRunId, aProcedure);

					if (aProcedure.getADependencyResultList().size() == 0) {
						dependencyAnalyzerController.insertErrors(nextRunId, aDBObject.getOWNER(),
								aDBObject.getOBJECT_NAME(), "NODEPENDENCY",null);
					}

				} catch (Exception e) {

					errorCount++;
					errorProcs.add(new ErrorInfo(aDBObject.getOBJECT_NAME(),e.toString().substring(0, e.toString().length()-1)));

					e.printStackTrace();
				}

				System.out.println("END OBJECT: " + aDBObject.getOBJECT_NAME());
			}

			for (ErrorInfo errorProc : errorProcs) {

				
				dependencyAnalyzerController.insertErrors(nextRunId, objectOwner, errorProc.getObjectName(), "ERROR",errorProc.getError());

			}

			dependencyAnalyzerController.updateSchemaInformation(nextRunId);
			dependencyAnalyzerController.updateRunInformation(nextRunId);

		} else if (runType == 3) // ALL Schemas Run
		{
			
			List<ErrorInfo> errorProcs = null;
			
			dependencyAnalyzerController.insertRunInformation(nextRunId, "DATABASE", null, null);

			List<DBSchema> dBSchemaList = dependencyAnalyzerController.findSchemas(null);
			for (DBSchema dbSchema : dBSchemaList) {
				
				System.out.println("START SCHEMA: " + dbSchema.getOWNER());
				
				errorProcs = new ArrayList<ErrorInfo>();

				dependencyAnalyzerController.insertSchemaInformation(nextRunId, dbSchema.getOWNER(),
						dbSchema.getOBJECTCOUNT());

				List<DBObject> res = dependencyAnalyzerController.findProcs(dbSchema.getOWNER());
				for (DBObject aDBObject : res) {

					System.out.println("START OBJECT: " + aDBObject.getOBJECT_NAME());

					try {
						Procedure aProcedure = dependencyAnalyzerController.analyzeDependency(aDBObject);
						dependencyAnalyzerController.insertResult(nextRunId, aProcedure);

						if (aProcedure.getADependencyResultList().size() == 0) {
							dependencyAnalyzerController.insertErrors(nextRunId,dbSchema.getOWNER(),
									aDBObject.getOBJECT_NAME(), "NODEPENDENCY",null);
						}

					} catch (Exception e) {

						errorProcs.add(new ErrorInfo(aDBObject.getOBJECT_NAME(),e.toString().substring(0, e.toString().length()-1)));


						e.printStackTrace();
					}

					System.out.println("END OBJECT: " + aDBObject.getOBJECT_NAME());
				}

				for (ErrorInfo errorProc : errorProcs) {

					dependencyAnalyzerController.insertErrors(nextRunId, dbSchema.getOWNER(), errorProc.getObjectName(), "ERROR",errorProc.getError());

				}

				dependencyAnalyzerController.updateSchemaInformation(nextRunId);
				
				System.out.println("END SCHEMA: " + dbSchema.getOWNER());


			}

			dependencyAnalyzerController.updateRunInformation(nextRunId);

		} 
		
		else {
			System.out.println("INVALID RUN TYPE");
		}

		System.out.println("DEPENDENCY ANALYZER ENDED");

	}

}
