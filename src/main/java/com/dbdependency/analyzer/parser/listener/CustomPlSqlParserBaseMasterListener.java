
package com.dbdependency.analyzer.parser.listener;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import com.dbdependency.analyzer.model.Statement;
import com.dbdependency.analyzer.parser.PlSqlParser;
import com.dbdependency.analyzer.parser.PlSqlParserBaseListener;
import com.dbdependency.analyzer.parser.core.DependencyAnalyzer;

public class CustomPlSqlParserBaseMasterListener extends PlSqlParserBaseListener {

	// holds all statements with listener method names
	public List<Statement> allStatements = new ArrayList<Statement>();

	@Override
	public void enterExecute_immediate(PlSqlParser.Execute_immediateContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		
		DependencyAnalyzer.collectAllStatements("enterExecute_immediate", children, listenerTerminals,allStatements);

	}

	
	@Override
	public void enterVariable_declaration(PlSqlParser.Variable_declarationContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.collectAllStatements("enterVariable_declaration", children, listenerTerminals,allStatements);

	}

	@Override
	public void enterAssignment_statement(PlSqlParser.Assignment_statementContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.collectAllStatements("enterAssignment_statement", children, listenerTerminals,allStatements);

	}

	@Override
	public void enterInsert_statement(PlSqlParser.Insert_statementContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.collectAllStatements("enterInsert_statement", children, listenerTerminals,allStatements);
	}

	@Override
	public void enterUpdate_statement(PlSqlParser.Update_statementContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.collectAllStatements("enterUpdate_statement", children, listenerTerminals,allStatements);

	}

	@Override
	public void enterDelete_statement(PlSqlParser.Delete_statementContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.collectAllStatements("enterDelete_statement", children, listenerTerminals,allStatements);
	}

	@Override
	public void enterMerge_statement(PlSqlParser.Merge_statementContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.collectAllStatements("enterMerge_statement", children, listenerTerminals,allStatements);
	}

	@Override
	public void enterSubquery(PlSqlParser.SubqueryContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.collectAllStatements("enterSubquery", children, listenerTerminals,allStatements);
	}

	@Override
	public void enterCursor_manipulation_statements(PlSqlParser.Cursor_manipulation_statementsContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.collectAllStatements("enterCursor_manipulation_statements", children, listenerTerminals,allStatements);

	}



}
