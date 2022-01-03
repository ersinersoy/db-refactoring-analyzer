package com.dbdependency.analyzer.parser.util;

import com.dbdependency.analyzer.model.Procedure;
import com.dbdependency.analyzer.model.result.DependencyResult;

public class DependencyAnalyzerUtil {

	
	public static void printSimpleDependencyResult(Procedure aProcedure )
	{
		System.out.println("\nOBJECT NAME: "+aProcedure.getName());
		System.out.println("OBJECT SCHEMA NAME: "+aProcedure.getSchema().getName());
		System.out.println("\nDEPENDENCY RESULTS\n");
		for (DependencyResult aDependencyResult : aProcedure.getADependencyResultList()) {
			System.out.println(aDependencyResult);
		}
	}
}
