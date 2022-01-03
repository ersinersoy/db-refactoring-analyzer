
package com.dbdependency.analyzer.parser.core;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;

import com.dbdependency.analyzer.SpringContext;
import com.dbdependency.analyzer.controller.DependencyAnalyzerController;
import com.dbdependency.analyzer.db.model.DBColumn;
import com.dbdependency.analyzer.db.model.DBSQLResult;
import com.dbdependency.analyzer.db.model.DBTableView;
import com.dbdependency.analyzer.model.Assignment;
import com.dbdependency.analyzer.model.Column;
import com.dbdependency.analyzer.model.Cursor;
import com.dbdependency.analyzer.model.ExecuteImmediate;
import com.dbdependency.analyzer.model.Procedure;
import com.dbdependency.analyzer.model.Schema;
import com.dbdependency.analyzer.model.Statement;
import com.dbdependency.analyzer.model.TableView;
import com.dbdependency.analyzer.model.Variable;
import com.dbdependency.analyzer.model.context.AllColumnSelectContext;
import com.dbdependency.analyzer.model.context.Context;
import com.dbdependency.analyzer.model.context.ParsedTableView;
import com.dbdependency.analyzer.model.context.RegularIdContext;
import com.dbdependency.analyzer.model.context.TableAliasContext;
import com.dbdependency.analyzer.model.context.TableViewContext;
import com.dbdependency.analyzer.model.result.DependencyResult;
import com.dbdependency.analyzer.parser.CaseChangingCharStream;
import com.dbdependency.analyzer.parser.PlSqlLexer;
import com.dbdependency.analyzer.parser.PlSqlParser;
import com.dbdependency.analyzer.parser.PlSqlParser.Select_list_elementsContext;
import com.dbdependency.analyzer.parser.PlSqlParser.Selected_listContext;
import com.dbdependency.analyzer.parser.PlSqlParser.Table_aliasContext;
import com.dbdependency.analyzer.parser.PlSqlParser.Tableview_nameContext;
import com.dbdependency.analyzer.parser.listener.CustomPlSqlParserBaseDetailListener;
import com.dbdependency.analyzer.parser.listener.CustomPlSqlParserBaseMasterListener;
import com.dbdependency.analyzer.parser.listener.CustomPlSqlParserSQLAsResultDetatilListener;
import com.dbdependency.analyzer.parser.util.PropertiesCache;

public class DependencyAnalyzer {

	static boolean SYSTEM_OUTPUT = Boolean.parseBoolean(PropertiesCache.getInstance().getProperty("app.system.out"));

	public static void findAllTerminalNodes(List<ParseTree> children, List<TerminalNodeImpl> listenerTerminals) {
		for (ParseTree parseTree : children) {
			if (parseTree instanceof TerminalNodeImpl) {

				listenerTerminals.add((TerminalNodeImpl) parseTree);

			} else {
				int childCount = parseTree.getChildCount();
				List<ParseTree> tempParseTree = new ArrayList<ParseTree>();

				for (int i = 0; i < childCount; i++) {
					tempParseTree.add(parseTree.getChild(i));

				}
				findAllTerminalNodes(tempParseTree, listenerTerminals);
			}
		}
	}

	public static void collectAllStatements(String listenerMethodName, List<ParseTree> children,
			List<TerminalNodeImpl> listenerTerminals, List<Statement> allStatements) {

		String statementWithBracketString = "";
		String pureStatementString = "";
		int statementLine = -1;

		findAllTerminalNodes(children, listenerTerminals);

		String terminalText = "";

		for (TerminalNodeImpl aTerminalNodeImpl : listenerTerminals) {

			terminalText = aTerminalNodeImpl.getText();

			if (statementWithBracketString.equalsIgnoreCase("")) // First Terminal
			{
				statementLine = aTerminalNodeImpl.getSymbol().getLine();
			} else {
				statementWithBracketString = statementWithBracketString + "_#_"; // Adding separator for end of each
																					// terminal
				pureStatementString = pureStatementString + " "; // Adding separator for end of each terminal
			}

			terminalText = terminalText.replace("\t", " ").replace("\n", " ").replace("\r", " ");
			statementWithBracketString = statementWithBracketString + terminalText.toUpperCase();
			pureStatementString = pureStatementString + terminalText.toUpperCase();

		}

		allStatements
				.add(new Statement(listenerMethodName, statementWithBracketString, statementLine, pureStatementString));
	}

	public static void searchForAllColumnSelectContext(List<ParseTree> children,
			List<TerminalNodeImpl> listenerTerminals, List<Context> contextResults) throws Exception {

		findAllTerminalNodes(children, listenerTerminals);

		String terminalText = "";

		AllColumnSelectContext aAllColumnSelectContext = null;

		for (int i = 0; i < listenerTerminals.size(); i++) {

			TerminalNodeImpl aTerminalNodeImpl = listenerTerminals.get(i);
			terminalText = aTerminalNodeImpl.getText();

			if (terminalText.equalsIgnoreCase("*")
					&& (aTerminalNodeImpl.getParent() instanceof Select_list_elementsContext
							|| aTerminalNodeImpl.getParent() instanceof Selected_listContext)) {

				aAllColumnSelectContext = new AllColumnSelectContext();

				// if Previous terminal is . search for alias or table/view name
				if (((i - 1) >= 0) && listenerTerminals.get(i - 1) != null
						&& listenerTerminals.get(i - 1).getText().equalsIgnoreCase(".")) {

					if ((i - 2) >= 0) {
						aAllColumnSelectContext.setValue(listenerTerminals.get(i - 2).getText());
					} else {
						throw new Exception("All Column select'in alias ya da tablosu bulunamadı");
					}

				} else {
					aAllColumnSelectContext.setValue("*"); // No alias case;
				}

				contextResults.add(aAllColumnSelectContext);
			}

		}

	}

	public static int checkParentForRegularId(ParseTree aTerminalNodeImpl) {

		int result = -1;

		if (aTerminalNodeImpl == null || aTerminalNodeImpl.getParent() == null) {
			result = 1;
			return result;
		} else {
			if (aTerminalNodeImpl.getParent() instanceof Tableview_nameContext
					|| aTerminalNodeImpl.getParent() instanceof Table_aliasContext) {
				result = 0;
				return result;
			} else {
				result = checkParentForRegularId(aTerminalNodeImpl.getParent());
				if (result != -1) {
					return result;
				}
			}
		}
		return result;

	}

	public static int checkParentForTableView(ParseTree aTerminalNodeImpl) {

		int result = -1;

		if (aTerminalNodeImpl == null || aTerminalNodeImpl.getParent() == null) {
			result = 1;
			return result;
		} else {
			if (aTerminalNodeImpl.getParent() instanceof Select_list_elementsContext) {
				result = 0;
				return result;
			} else {
				result = checkParentForTableView(aTerminalNodeImpl.getParent());
				if (result != -1) {
					return result;
				}
			}
		}
		return result;

	}

	public static void searchForRegularIdContext(List<ParseTree> children, List<TerminalNodeImpl> listenerTerminals,
			List<Context> contextResults) {

		findAllTerminalNodes(children, listenerTerminals);

		String terminalText = "";

		RegularIdContext aRegularIdContext = null;

		for (int i = 0; i < listenerTerminals.size(); i++) {

			TerminalNodeImpl aTerminalNodeImpl = listenerTerminals.get(i);
			terminalText = aTerminalNodeImpl.getText();

			int checkParent = checkParentForRegularId(aTerminalNodeImpl);

			if (checkParent == 1 && !terminalText.equalsIgnoreCase("ROWNUM")) {

				aRegularIdContext = new RegularIdContext();
				aRegularIdContext.setValue(terminalText);

				contextResults.add(aRegularIdContext);
			}

		}

	}

	public static void searchForTableViewContext(List<ParseTree> children, List<TerminalNodeImpl> listenerTerminals,
			List<Context> contextResults) {

		findAllTerminalNodes(children, listenerTerminals);

		String terminalText = "";

		TableViewContext aTableViewContext = null;

		for (int i = 0; i < listenerTerminals.size(); i++) {

			TerminalNodeImpl aTerminalNodeImpl = listenerTerminals.get(i);

			terminalText = aTerminalNodeImpl.getText();

			int checkParent = checkParentForTableView(aTerminalNodeImpl);

			if (checkParent == 1) {
				aTableViewContext = new TableViewContext();
				aTableViewContext.setValue(terminalText);

				contextResults.add(aTableViewContext);

			}

		}

	}

	public static void searchForTableAliasContext(List<ParseTree> children, List<TerminalNodeImpl> listenerTerminals,
			List<Context> contextResults) {

		findAllTerminalNodes(children, listenerTerminals);

		String terminalText = "";

		TableAliasContext aTableAliasContext = null;

		for (int i = 0; i < listenerTerminals.size(); i++) {

			TerminalNodeImpl aTerminalNodeImpl = listenerTerminals.get(i);

			terminalText = aTerminalNodeImpl.getText();

			aTableAliasContext = new TableAliasContext();
			aTableAliasContext.setValue(terminalText);

			contextResults.add(aTableAliasContext);

		}

	}

	public static void addUnHandledCasesToDependencyList(boolean aMultibleAssignmentExist,
			boolean aComplexAssignmentExist, boolean aStringConcatenationExist, boolean aTableNotCatched,
			String Operation, Statement aStatement, List<DependencyResult> aDependencyResultList) {

		aDependencyResultList.add(new DependencyResult(aStatement.getLine(), Operation, "NA", "NA", "NA", aStatement,
				aMultibleAssignmentExist, aComplexAssignmentExist, aStringConcatenationExist, aTableNotCatched, true));
	}

	public static void findOperationLevelDependency(String SQL, Map<String, TableView> spTableView,
			Map<String, Column> spTableViewColumns, String ProcedureOwner, String Operation, Statement aStatement,
			List<DependencyResult> aDependencyResultList, boolean isDummyCall) throws Exception {

		CharStream s;
		CaseChangingCharStream upper = null;
		s = CharStreams.fromString(SQL);

		upper = new CaseChangingCharStream(s, true);

		PlSqlLexer lexer = new PlSqlLexer(upper);

		PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));

		parser.setErrorHandler(new BailErrorStrategy());

		ParseTree tree = parser.sql_script();

		ParseTreeWalker walker = new ParseTreeWalker();

		CustomPlSqlParserBaseDetailListener listener = new CustomPlSqlParserBaseDetailListener();

		walker.walk(listener, tree);

		List<Context> allContextResults = listener.contextResults;
		String DEFAULT_OWNER = ProcedureOwner;
		String OPERATION = Operation;
		int LINE = aStatement.getLine();

		int initialDependencyResultSize = aDependencyResultList.size();

		if (SYSTEM_OUTPUT) {
			for (Context aContext : allContextResults) {
				System.out.println(aContext.getValue() + " " + aContext.getClass());
			}
		}

		Map<String, String> contextResultAliasMap = new LinkedHashMap<String, String>();

		// Get Table Alias
		for (Context aContext : allContextResults) {

			if (aContext instanceof TableAliasContext) {
				contextResultAliasMap.put(aContext.getValue(), aContext.getValue());
			}
		}

		Context aContext = null;
		MultiKeyMap<String, ParsedTableView> aParsedTableViewMap = MultiKeyMap.multiKeyMap(new LinkedMap());
		Map<String, ParsedTableView> aParsedTableViewUnique = new LinkedHashMap<String, ParsedTableView>();

		// Get Tables
		for (int i = 0; i < allContextResults.size(); i++) {

			aContext = allContextResults.get(i);

			if (aContext instanceof TableViewContext) {

				// allContextResults.size()> (i + 1) for outofbounds exception

				if (((allContextResults.size() > (i + 1)) && allContextResults.get(i + 1) != null
						&& allContextResults.get(i + 1).getValue().equalsIgnoreCase("."))
						|| aContext.getValue().equalsIgnoreCase(".")) {

					// If TableViewContext Owner or . ignore

				} else {

					if (!spTableView.containsKey(aContext.getValue())) {

						// Dinamik SQL spTable and spTableColumns extension

						if (isDummyCall) {

							// null

						} else {
							DependencyAnalyzerController aDenemeController = SpringContext
									.getBean(DependencyAnalyzerController.class);

							aDenemeController.extendTableViewColumnMap(aContext.getValue(), spTableViewColumns,
									spTableView);

						}
					}

					String tableOwner = null;
					String tableAlias = null;

					String tableName = aContext.getValue();

					// ((i - 1)>=0) &&
					// ((i - 2)>=0) &&

					// Get Table Owner if exist
					if (((i - 1) >= 0) && allContextResults.get(i - 1) != null
							&& allContextResults.get(i - 1).getValue().equalsIgnoreCase(".")
							&& allContextResults.get(i - 1) instanceof TableViewContext) {

						if (((i - 2) >= 0) && allContextResults.get(i - 2) != null
								&& allContextResults.get(i - 2) instanceof TableViewContext) // OWNER
						{
							tableOwner = allContextResults.get(i - 2).getValue();
						} else {
							throw new Exception("tableOwner could not be found, should be exist");
						}

					}

					// Get Alias if
					if ((allContextResults.size() > (i + 1)) && allContextResults.get(i + 1) != null
							&& allContextResults.get(i + 1) instanceof TableAliasContext) {
						tableAlias = allContextResults.get(i + 1).getValue();
					}

					if (tableOwner == null) {
						tableOwner = DEFAULT_OWNER;
					}

					// tableOwner always not null ; 3 key for search only alias or tablename or both
					aParsedTableViewMap.put(tableOwner, tableAlias, tableName,
							new ParsedTableView(tableAlias, tableOwner, tableName));

					aParsedTableViewMap.put(tableOwner, tableAlias, null,
							new ParsedTableView(tableAlias, tableOwner, tableName));
					aParsedTableViewMap.put(null, tableAlias, null,
							new ParsedTableView(tableAlias, tableOwner, tableName));

					aParsedTableViewMap.put(tableOwner, null, tableName,
							new ParsedTableView(tableAlias, tableOwner, tableName));
					aParsedTableViewMap.put(null, null, tableName,
							new ParsedTableView(tableAlias, tableOwner, tableName));

					// it for all column select without alias case
					aParsedTableViewUnique.put(tableName, new ParsedTableView(tableAlias, tableOwner, tableName));

				}

			}

		}

		// Latest operation Find the dependencies
		for (int i = 0; i < allContextResults.size(); i++) {

			aContext = allContextResults.get(i);

			if (aContext instanceof AllColumnSelectContext) {

				if (aContext.getValue().equalsIgnoreCase("*")) {
					// All columns select from all tables
					for (Map.Entry<String, ParsedTableView> aParsedTable : aParsedTableViewUnique.entrySet()) {

						aDependencyResultList.add(new DependencyResult(LINE, OPERATION, "*",
								aParsedTable.getValue().getName(), aParsedTable.getValue().getOwner(), aStatement,
								false, false, false, false, false));
					}

				} else {
					ParsedTableView aParsedTable = null;
					if (aParsedTableViewMap.containsKey(null, null, aContext.getValue())) {
						// From Table Name
						aParsedTable = aParsedTableViewMap.get(null, null, aContext.getValue());
						aDependencyResultList.add(new DependencyResult(LINE, OPERATION, "*", aParsedTable.getName(),
								aParsedTable.getOwner(), aStatement, false, false, false, false, false));

					} else if (aParsedTableViewMap.containsKey(null, aContext.getValue(), null)) {
						aParsedTable = aParsedTableViewMap.get(null, aContext.getValue(), null);
						aDependencyResultList.add(new DependencyResult(LINE, OPERATION, "*", aParsedTable.getName(),
								aParsedTable.getOwner(), aStatement, false, false, false, false, false));

					} else {
						// * select table information not found (Select a,b,c from y) k & select k.*
						// case
						aDependencyResultList.add(new DependencyResult(LINE, OPERATION, "*", null, null, aStatement,
								false, false, false, false, false));
					}

				}

			} else if (aContext instanceof RegularIdContext) {

				if (spTableViewColumns.containsKey(aContext.getValue())) // Kolon ise
				{

					Column aColumn = spTableViewColumns.get(aContext.getValue());
					Map<String, TableView> aTableViewMap = aColumn.getTableView();

					// Map<String,TableView> aSchemaMap = aTableViewMap.g

					TableView aTableView = null;
					Schema aSchema = null;
					ParsedTableView aParsedTableView = null;

					if (!aColumn.isMultibleTableViewExist()) // Column exist only single table
					{

						for (String keyTable : aColumn.getTableView().keySet()) {
							aTableView = aColumn.getTableView().get(keyTable);

							for (String keySchema : aTableView.getSchema().keySet()) {

								aSchema = aTableView.getSchema().get(keySchema);
							}
						}

						aDependencyResultList
								.add(new DependencyResult(LINE, OPERATION, aColumn.getName(), aTableView.getName(),
										aSchema.getName(), aStatement, false, false, false, false, false));
					} else // Column exist more than one table in which procedure depdendent 
					{

						int numberOfUsedTableOnOperation = 0;

						for (String keyTableUnique : aParsedTableViewUnique.keySet()) {

							if (aTableViewMap.containsKey(keyTableUnique)) {
								numberOfUsedTableOnOperation++;
								aParsedTableView = aParsedTableViewUnique.get(keyTableUnique);
								aTableView = aColumn.getTableView().get(keyTableUnique);
							}

						}

						if (numberOfUsedTableOnOperation == 1) {
							/* Column exist more than one table in which procedure depdendent 
							but PL/SQL statement level only one table used*/

							aParsedTableView = aParsedTableViewMap.get(null, null, aTableView.getName());

							aDependencyResultList.add(new DependencyResult(LINE, OPERATION, aColumn.getName(),
									aParsedTableView.getName(), aParsedTableView.getOwner(), aStatement, false, false,
									false, false, false));

						} else if (numberOfUsedTableOnOperation > 1) {

							aParsedTableView = null;
							aTableView = null;

							/* Column exist more than one table in which procedure depdendent 
							and PL/SQL statement level used more than one table. 
							There should be alias for this case. It is find out here.	
							*/


							if ((i - 1) >= 0 && allContextResults.get(i - 1) != null
									&& contextResultAliasMap.containsKey(allContextResults.get(i - 1).getValue())) {
								// Previous regular id is alias. Confirmed here. 

								aParsedTableView = aParsedTableViewMap.get(null,
										allContextResults.get(i - 1).getValue(), null);

								if (aParsedTableView != null) {

									aDependencyResultList.add(new DependencyResult(LINE, OPERATION, aColumn.getName(),
											aParsedTableView.getName(), aParsedTableView.getOwner(), aStatement, false,
											false, false, false, false));
								} else {
									
								}

							} else if ((i - 1) >= 0 && allContextResults.get(i - 1) != null && aParsedTableViewMap
									.containsKey(null, null, allContextResults.get(i - 1).getValue())) {
								/*
								 Event very rare case, there is possibility directly table name
								 is used instead of alias. Like CUSTOMERTABLE.CUSTOMERID etc.
								 */

								aParsedTableView = aParsedTableViewMap.get(null, null,
										allContextResults.get(i - 1).getValue());

								aDependencyResultList.add(new DependencyResult(LINE, OPERATION, aColumn.getName(),
										aParsedTableView.getName(), aParsedTableView.getOwner(), aStatement, false,
										false, false, false, false));

							} else {

								 // For Merge, table not catched case.
								if (OPERATION.equalsIgnoreCase("MERGE"))									
								{
									aDependencyResultList.add(new DependencyResult(LINE, OPERATION, aColumn.getName(), "NA",
											"NA", aStatement, false, false, false, true, true));
									
								}

							}

						} else {
							
						}

					}
				} else {
					// Ignore it is not a column
				}

			} else {
				// Ignore other contexts
			}

		}

		if (SYSTEM_OUTPUT) {

			System.out.println("Dependency Result:");

			for (int i = initialDependencyResultSize; i < aDependencyResultList.size(); i++) {
				System.out.println(aDependencyResultList.get(i));

			}
		}

	}

	public static Assignment assignmentOperation(String listenerName, Statement aStatement, String[] arrOfStr,
			int astatementline, Assignment aAssignment) {
		for (int i = 1; i < arrOfStr.length; i++) { // Start with i=1 because first array is variable name

			boolean isComplexAssignment = false;

			if (arrOfStr[i - 1].equalsIgnoreCase(":=") || arrOfStr[i - 1].equalsIgnoreCase("DEFAULT")) {

				aAssignment = new Assignment();
				aAssignment.setStatement(aStatement);
				aAssignment.setLine(astatementline);

				if ((i == arrOfStr.length - 1 && listenerName == "enterAssignment_statement")
						|| (i == arrOfStr.length - 2 && listenerName == "enterVariable_declaration"
								&& arrOfStr[arrOfStr.length - 1].equalsIgnoreCase(";"))) {
					isComplexAssignment = false;

				} else {
					isComplexAssignment = true;

				}

				if (isComplexAssignment) {
					aAssignment.setValue("-1"); // ComplexAssignment
				} else {
					aAssignment.setValue(arrOfStr[i]);
				}

			}

		}
		return aAssignment;
	}

	public static Map<String, TableView> fillTableViewMap(Map<String, Schema> spSchema,
			List<DBTableView> dbaTableViewList) {

		Map<String, TableView> spTableView = new LinkedHashMap<String, TableView>();

		for (DBTableView dbaTableView : dbaTableViewList) {

			if (spTableView.containsKey(dbaTableView.getName())) {
				TableView aTempTableView = spTableView.get(dbaTableView.getName());
				Map<String, Schema> aTempSchemaMap = aTempTableView.getSchema();
				aTempSchemaMap.put(dbaTableView.getSchema(), new Schema(dbaTableView.getSchema(), null));
				aTempTableView.setMultibleSchemaExist(true);
			} else {
				Schema aTempSchema = new Schema(dbaTableView.getSchema(), null);
				Map<String, Schema> aTempSchemaMap = new LinkedHashMap<String, Schema>();
				aTempSchemaMap.put(dbaTableView.getSchema(), aTempSchema);
				TableView aTempTableView = new TableView(dbaTableView.getName(), 0, false, aTempSchemaMap,
						dbaTableView.getType());
				spTableView.put(dbaTableView.getName(), aTempTableView);

			}

			spSchema.put(dbaTableView.getSchema(), new Schema(dbaTableView.getSchema(), null));

		}

		return spTableView;

	}

	public static Map<String, Column> fillColumnMap(List<DBColumn> dbaColumnList) {

		Map<String, Column> spTableViewColumns = new LinkedHashMap<String, Column>();

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

		return spTableViewColumns;

	}

	public static Procedure findPLSQLdependency(List<Statement> allStatements, Map<String, TableView> spTableView,
			Map<String, Column> spTableViewColumns, Procedure aProcedure, Map<String, Variable> avariableMap,
			List<Assignment> aAssignmentList, List<DependencyResult> aDependencyResultList, boolean isDummyRun)
			throws Exception {

		for (int statementIndice = 0; statementIndice < allStatements.size(); statementIndice++) {

			Statement aStatement = allStatements.get(statementIndice);

			if (SYSTEM_OUTPUT) {
				System.out.println("*****" + aStatement);

			}

			String[] arrOfStr = aStatement.getStatementWithBracket().split("_#_", -2);

			if (SYSTEM_OUTPUT) {

				for (String a : arrOfStr) {

					System.out.println(a);
				}
			}

			int astatementline = aStatement.getLine();

			// Just for debugging from specific statement line.
			if (astatementline == 20 && SYSTEM_OUTPUT) {
				System.out.println("HERE");
			}

			Variable aVariable = null;
			Assignment aAssignment = null;
			aAssignmentList = null;
			Variable checkVariableUsed = null;

			boolean aMultibleAssignmentExist = false;
			boolean aComplexAssignmentExist = false;
			boolean aStringConcatenationExist = false;
			boolean aTableNotCatched = false;

			switch (aStatement.getListenerMethodName()) {

			case "enterVariable_declaration":

				aVariable = new Variable();
				aVariable.setStatement(aStatement);
				aVariable.setLine(astatementline);
				aVariable.setName(arrOfStr[0]);

				avariableMap.put(aVariable.getName(), aVariable);

				// assignmentOperation done via assignmentOperation method
				aAssignment = DependencyAnalyzer.assignmentOperation("enterVariable_declaration", aStatement, arrOfStr,
						astatementline, aAssignment);

				if (aAssignment != null) {
					aAssignmentList = new ArrayList<Assignment>();
					aAssignmentList.add(aAssignment);
					aVariable.setAssignments(aAssignmentList);

					if (SYSTEM_OUTPUT) {
						System.out.println("ASSIGNMENT: " + aVariable.getAssignments().get(0).getValue());

					}

				}

				if (SYSTEM_OUTPUT) {
					System.out.println("VARIABLE: " + aVariable.getName());

				}

				break;

			case "enterAssignment_statement":

				aVariable = avariableMap.get(arrOfStr[0]);

				if (aVariable == null) // Procedure / Function IN OUT parameter
				{
					aVariable = new Variable();
				}

				aAssignmentList = aVariable.getAssignments();
				if (aAssignmentList == null) {
					aAssignmentList = new ArrayList<Assignment>();
				} else {
					aVariable.setMultiValueExist(true);

				}

				// assignmentOperation done via assignmentOperation method
				aAssignment = DependencyAnalyzer.assignmentOperation("enterAssignment_statement", aStatement, arrOfStr,
						astatementline, aAssignment);
				aAssignmentList.add(aAssignment);
				aVariable.setAssignments(aAssignmentList);

				if (SYSTEM_OUTPUT) {
					System.out.println("XXX ASSIGNMENT:"
							+ aVariable.getAssignments().get(aVariable.getAssignments().size() - 1).getValue());

				}

				break;

			case "enterExecute_immediate":

				ExecuteImmediate aExecuteImmediate = new ExecuteImmediate();

				String aDynamicString = arrOfStr[2];

				if (aDynamicString.equalsIgnoreCase("(")) // ( case before SQL
				{
					aDynamicString = arrOfStr[3];
				}

				// Multible string concatenation case flag
				for (int i = 3; i < arrOfStr.length; i++) {

					if (arrOfStr[i].equalsIgnoreCase("USING")) {

						break;

					} else {

						if (arrOfStr[i].equalsIgnoreCase("|")) {
							aStringConcatenationExist = true;
						}

					}

				}

				// Array Variable case flag
				for (int i = 3; i < arrOfStr.length; i++) {

					if (arrOfStr[i].equalsIgnoreCase("USING")) {

						break;

					} else {

						if (arrOfStr[i].equalsIgnoreCase(".")) {
							aMultibleAssignmentExist = true; // Multible SQL such as l.sql inside FOR LOOP
						}

					}

				}

				checkVariableUsed = avariableMap.get(aDynamicString);

				// Multible assignment exist flag
				if (checkVariableUsed != null && checkVariableUsed.isMultiValueExist()) {
					aMultibleAssignmentExist = true;
				}

				if (aStringConcatenationExist == false && checkVariableUsed != null) {
					aExecuteImmediate.setVariable(checkVariableUsed);
					aExecuteImmediate.setVariableUsed(true);
					if (checkVariableUsed.getAssignments() != null) {

						// More than one assignment to same variable. Latest assignment used
						for (int j = checkVariableUsed.getAssignments().size() - 1; j >= 0; j--) {

							String jVariableValue = checkVariableUsed.getAssignments().get(j).getValue();
							if (!jVariableValue.equalsIgnoreCase("-1")) {
								aExecuteImmediate.setValue(jVariableValue);
								break;
							} else {
								aComplexAssignmentExist = true; // Complex assignment exist
								aExecuteImmediate.setValue(null);
							}

						}

					} else {

						if (!aStringConcatenationExist && !aMultibleAssignmentExist) {

							aComplexAssignmentExist = dynamicSQLAsAResultCaseForCursorAndExecuteImmediate(allStatements, spTableView,
									spTableViewColumns, aProcedure, avariableMap, aAssignmentList,
									aDependencyResultList, isDummyRun, statementIndice, aStatement, checkVariableUsed,
									aComplexAssignmentExist);
						} else {
							// aStringConcatenationExist unhandled record will be added to dependency result
						}

					}
				} else {

					if (!aStringConcatenationExist && !aMultibleAssignmentExist) {
						aExecuteImmediate.setValue(aDynamicString);
					} else {
						aExecuteImmediate.setValue(null);
					}
				}

				if (aExecuteImmediate.getValue() != null) {

					String forremovingSingleQoute = aExecuteImmediate.getValue();
					// removing '
					forremovingSingleQoute = forremovingSingleQoute.substring(1, forremovingSingleQoute.length() - 1);

					// there is a case '' if the SQL declared in quotes, so we are replacing '' to '
					forremovingSingleQoute = forremovingSingleQoute.replace("''", "'");

					// Dynamic SQL
					if (forremovingSingleQoute.startsWith("SELECT") || forremovingSingleQoute.startsWith("INSERT")
							|| forremovingSingleQoute.startsWith("UPDATE")
							|| forremovingSingleQoute.startsWith("DELETE")
							|| forremovingSingleQoute.startsWith("MERGE")) {

						DependencyAnalyzer.findOperationLevelDependency(forremovingSingleQoute, spTableView,
								spTableViewColumns, aProcedure.getSchema().getName(), "EXECUTE IMMEDIATE", aStatement,
								aDependencyResultList, isDummyRun);
					}

				} else {
					
				}

				// Adding unhandledCased to Dependency Result
				if (aMultibleAssignmentExist || aComplexAssignmentExist || aStringConcatenationExist
						|| aTableNotCatched) {
					DependencyAnalyzer.addUnHandledCasesToDependencyList(aMultibleAssignmentExist,
							aComplexAssignmentExist, aStringConcatenationExist, aTableNotCatched, "EXECUTE IMMEDIATE",
							aStatement, aDependencyResultList);


				}

				break;

			case "enterCursor_manipulation_statements":

				Cursor aCursor = new Cursor();
				boolean isStandardCursor = false;

				// Multible string concatenation case flag
				for (int i = 4; i < arrOfStr.length; i++) {

					if (arrOfStr[i].equalsIgnoreCase("USING")) {

						break;

					} else {

						if (arrOfStr[i].equalsIgnoreCase("|")) {
							aStringConcatenationExist = true;
						}

					}

				}

				if (aStringConcatenationExist == false && arrOfStr.length >= 4 && arrOfStr[0].equalsIgnoreCase("OPEN")
						&& arrOfStr[2].equalsIgnoreCase("FOR")
						&& (arrOfStr.length == 4 || arrOfStr[4].equalsIgnoreCase("USING"))) {
					// Variable Case

					String aCursorVariable = arrOfStr[3];
					checkVariableUsed = avariableMap.get(aCursorVariable);

					// Multible assignment exist flag
					if (checkVariableUsed != null && checkVariableUsed.isMultiValueExist()) {
						aMultibleAssignmentExist = true;
					}

					if (checkVariableUsed != null) {
						aCursor.setVariable(checkVariableUsed);
						aCursor.setVariableUsed(true);
						if (checkVariableUsed.getAssignments() != null) {

							// More than one assignment to same variable. Latest assignment used
							for (int j = checkVariableUsed.getAssignments().size() - 1; j >= 0; j--) {

								String jVariableValue = checkVariableUsed.getAssignments().get(j).getValue();
								if (!jVariableValue.equalsIgnoreCase("-1")) {
									aCursor.setValue(jVariableValue);
									break;
								} else {
									aComplexAssignmentExist = true; // Complex assignment exist
									aCursor.setValue(null);
								}

							}

							aCursor.setValue(checkVariableUsed.getAssignments()
									.get(checkVariableUsed.getAssignments().size() - 1).getValue());
						} else {

							if (!aStringConcatenationExist && !aMultibleAssignmentExist) {

								aComplexAssignmentExist = dynamicSQLAsAResultCaseForCursorAndExecuteImmediate(allStatements, spTableView,
										spTableViewColumns, aProcedure, avariableMap, aAssignmentList,
										aDependencyResultList, isDummyRun, statementIndice, aStatement, checkVariableUsed,
										aComplexAssignmentExist);

							} else {
								// aStringConcatenationExist unhandled record will be added to dependency result
							}

						}
					} else if (aCursorVariable.startsWith("'")) // SELECT in qoutes ' SELECT * FROM ...'
					{

						// Removing '
						String forremovingSingleQoute = aCursorVariable.substring(1, aCursorVariable.length() - 1);

						// there is a case '' if the SQL declared in quotes, so we are replacing '' to '
						forremovingSingleQoute = forremovingSingleQoute.replace("''", "'");

						aCursor.setValue(forremovingSingleQoute);

					}

				} else if (aStringConcatenationExist == false && arrOfStr.length >= 4
						&& arrOfStr[0].equalsIgnoreCase("OPEN") && arrOfStr[2].equalsIgnoreCase("FOR")
						&& arrOfStr[3].equalsIgnoreCase("SELECT")) {
					// SQL Case SubSelect listener handle it so we ignore this case
					aCursor.setValue(null);

				} else {
					// Ignore not a open for cursor with dynamic SQL case.
					if (!aStringConcatenationExist && !aMultibleAssignmentExist) {
						isStandardCursor = true;
					} else {
						aCursor.setValue(null);
					}
				}

				if (aCursor.getValue() != null) {

					String cursorSQL = null;
					if (aCursor.isVariableUsed()) {
						cursorSQL = aCursor.getValue();
						// Removing '
						cursorSQL = cursorSQL.substring(1, cursorSQL.length() - 1);

					} else {
						cursorSQL = aCursor.getValue();
					}

					// there is a case '' if the SQL declared in quotes, so we are replacing '' to '
					cursorSQL = cursorSQL.replace("''", "'");

					DependencyAnalyzer.findOperationLevelDependency(cursorSQL, spTableView, spTableViewColumns,
							aProcedure.getSchema().getName(), "CURSOR", aStatement, aDependencyResultList, isDummyRun);

				} else {
					
					

				}

				// Adding unhandledCased to Dependency Result
				if (aMultibleAssignmentExist || aComplexAssignmentExist || aStringConcatenationExist
						|| aTableNotCatched) {
					DependencyAnalyzer.addUnHandledCasesToDependencyList(aMultibleAssignmentExist,
							aComplexAssignmentExist, aStringConcatenationExist, aTableNotCatched, "EXECUTE IMMEDIATE",
							aStatement, aDependencyResultList);

					if (SYSTEM_OUTPUT) {

						System.out.println(aDependencyResultList.get(aDependencyResultList.size() - 1));

					}
				}

				break;

			case "enterInsert_statement":

				DependencyAnalyzer.findOperationLevelDependency(aStatement.getPureStatement(), spTableView,
						spTableViewColumns, aProcedure.getSchema().getName(), "INSERT", aStatement,
						aDependencyResultList, isDummyRun);

				break;

			case "enterUpdate_statement":
				DependencyAnalyzer.findOperationLevelDependency(aStatement.getPureStatement(), spTableView,
						spTableViewColumns, aProcedure.getSchema().getName(), "UPDATE", aStatement,
						aDependencyResultList, isDummyRun);
				break;
			case "enterDelete_statement":
				DependencyAnalyzer.findOperationLevelDependency(aStatement.getPureStatement(), spTableView,
						spTableViewColumns, aProcedure.getSchema().getName(), "DELETE", aStatement,
						aDependencyResultList, isDummyRun);
				break;
			case "enterMerge_statement":
				DependencyAnalyzer.findOperationLevelDependency(aStatement.getPureStatement(), spTableView,
						spTableViewColumns, aProcedure.getSchema().getName(), "MERGE", aStatement,
						aDependencyResultList, isDummyRun);
				break;
			case "enterSubquery":
				DependencyAnalyzer.findOperationLevelDependency(aStatement.getPureStatement(), spTableView,
						spTableViewColumns, aProcedure.getSchema().getName(), "SELECT", aStatement,
						aDependencyResultList, isDummyRun);
				break;

			default:
				break;
			}

		}

		aProcedure.setAssignment(aAssignmentList);
		aProcedure.setVariable(avariableMap);
		aProcedure.setADependencyResultList(aDependencyResultList);

		return aProcedure;
	}

	public static boolean dynamicSQLAsAResultCaseForCursorAndExecuteImmediate(List<Statement> allStatements, Map<String, TableView> spTableView,
			Map<String, Column> spTableViewColumns, Procedure aProcedure, Map<String, Variable> avariableMap,
			List<Assignment> aAssignmentList, List<DependencyResult> aDependencyResultList, boolean isDummyRun,
			int statementIndice, Statement aStatement, Variable checkVariableUsed, boolean aComplexAssignmentExist) {
		if (!isDummyRun) {
			

			// Reverse loop from last statement-1 to find variable assignment value SELECT
			// statement
			for (int i = statementIndice - 1; i >= 0; i--) {

				Statement rStatement = allStatements.get(i);
				String rStatementPureStatement = rStatement.getPureStatement();

				if (rStatementPureStatement.startsWith("SELECT")
						&& rStatementPureStatement.contains(checkVariableUsed.getName())) {

					String before = org.apache.commons.lang3.StringUtils.substringBefore(
							rStatementPureStatement, "INTO " + checkVariableUsed.getName());
					String after = org.apache.commons.lang3.StringUtils.substringBetween(
							rStatementPureStatement, "INTO " + checkVariableUsed.getName(),
							"WHERE");
					String trimmedDynamicSQL = before + " RESULT " + after;

					if (SYSTEM_OUTPUT) {
						System.out.println("Trimmed SQL as a Result SQL :" + trimmedDynamicSQL);
					}

					try {

						List<Context> trimmedDynamicSQLContextResults = checkTrimmedSQLAndGetTableInformation(
								trimmedDynamicSQL);

						DependencyAnalyzerController aDenemeController = SpringContext
								.getBean(DependencyAnalyzerController.class);

						// It it used to find all owners of table
						for (Context trimmedSQLcontext : trimmedDynamicSQLContextResults) {

							String getOwnerSQL = "SELECT OWNER RESULT FROM DBA_OBJECTS WHERE OBJECT_NAME='"
									+ trimmedSQLcontext.getValue() + "'";

							List<DBSQLResult> trimmedSQLTableOwners = aDenemeController
									.getSQLResultList(getOwnerSQL);

							// Run SQL to get sql as a result SQLs for each owner
							for (DBSQLResult aTrimmedSQLTableOwner : trimmedSQLTableOwners) {

								// Schema Owner added to trimmedDynamicSQL Rownum limited to 1000 
								String trimmedDynamicSQLWithOwner = StringUtils.replace(
										trimmedDynamicSQL, trimmedSQLcontext.getValue(),
										aTrimmedSQLTableOwner.getResult() + "."
												+ trimmedSQLcontext.getValue()
												+ " WHERE ROWNUM<1001"); 

								List<DBSQLResult> trimmedSQLResults = aDenemeController
										.getSQLResultList(trimmedDynamicSQLWithOwner);

								for (DBSQLResult sqlAsAResults : trimmedSQLResults) {

									try {
										
									List<Statement> sqlAsAResultsStatements = DependencyAnalyzer
											.getParserResults(sqlAsAResults.getResult());

									// Set line number of statement negative
									for (Statement asqlAsAResultsStatements : sqlAsAResultsStatements) {
										asqlAsAResultsStatements.setLine(aStatement.getLine()*-1);
									}

									DependencyAnalyzer.findPLSQLdependency(sqlAsAResultsStatements,
											spTableView, spTableViewColumns, aProcedure,
											avariableMap, aAssignmentList, aDependencyResultList,
											isDummyRun);
									
									} catch (Exception e) {
										
										// Ignore same results could be not SQL if the table general purpose parameter table
									}

								}

							}

						}

					} catch (Exception e) {
						if (SYSTEM_OUTPUT) {
							System.out.println("Trimmed SQL dependency results are not catched :"
									+ trimmedDynamicSQL);
						}
						aComplexAssignmentExist = true;
					}

				}

			}

		} else {
			// isDummyRun true case
			


		}
		return aComplexAssignmentExist;
	}

	public static List<Statement> getParserResults(String code) {

		CharStream sourceCode = CharStreams.fromString(code);

		CaseChangingCharStream upperCaseSourceCode = new CaseChangingCharStream(sourceCode, true);

		return parseCode(upperCaseSourceCode);

	}

	public static List<Statement> parseCode(CaseChangingCharStream upperCaseSourceCode) {
		PlSqlLexer lexer = new PlSqlLexer(upperCaseSourceCode);

		PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));

		parser.setErrorHandler(new BailErrorStrategy());
		;

		ParseTree tree = parser.sql_script();

		ParseTreeWalker walker = new ParseTreeWalker();

		CustomPlSqlParserBaseMasterListener listener = new CustomPlSqlParserBaseMasterListener();

		walker.walk(listener, tree);

		List<Statement> allStatements = listener.allStatements;

		return allStatements;
	}

	public static List<Statement> getParserResultsFromFile(String filePath) {

		CharStream sourceCode = null;
		CaseChangingCharStream upperCaseSourceCode = null;
		try {
			sourceCode = CharStreams.fromPath(Paths.get(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		upperCaseSourceCode = new CaseChangingCharStream(sourceCode, true);

		return parseCode(upperCaseSourceCode);

	}

	public static List<Context> checkTrimmedSQLAndGetTableInformation(String trimmedDynamicSQL) {
		CharStream s;
		CaseChangingCharStream upper = null;
		s = CharStreams.fromString(trimmedDynamicSQL);

		upper = new CaseChangingCharStream(s, true);

		PlSqlLexer lexer = new PlSqlLexer(upper);

		PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));

		parser.setErrorHandler(new BailErrorStrategy());

		ParseTree tree = parser.sql_script();

		ParseTreeWalker walker = new ParseTreeWalker();

		CustomPlSqlParserSQLAsResultDetatilListener listener = new CustomPlSqlParserSQLAsResultDetatilListener();

		walker.walk(listener, tree);

		List<Context> allContextResults = listener.contextResults;
		return allContextResults;
	}

}
