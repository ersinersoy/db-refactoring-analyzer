
package com.dbdependency.analyzer.parser.listener;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import com.dbdependency.analyzer.model.context.Context;
import com.dbdependency.analyzer.parser.PlSqlParser;
import com.dbdependency.analyzer.parser.PlSqlParserBaseListener;
import com.dbdependency.analyzer.parser.core.DependencyAnalyzer;

public class CustomPlSqlParserBaseDetailListener extends PlSqlParserBaseListener {

	public List<Context> contextResults = new ArrayList<Context>();
	

	@Override public void enterTable_alias(PlSqlParser.Table_aliasContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.searchForTableAliasContext(children, listenerTerminals, contextResults);

	}


	@Override
	public void enterTableview_name(PlSqlParser.Tableview_nameContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.searchForTableViewContext(children, listenerTerminals, contextResults);

	}

	@Override
	public void enterRegular_id(PlSqlParser.Regular_idContext ctx) {

		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		DependencyAnalyzer.searchForRegularIdContext(children, listenerTerminals, contextResults);

	}

	@Override public void enterSelected_list(PlSqlParser.Selected_listContext ctx) {
		
		
		List<ParseTree> children = ctx.children;
		List<TerminalNodeImpl> listenerTerminals = new ArrayList<TerminalNodeImpl>();

		try {
			DependencyAnalyzer.searchForAllColumnSelectContext(children, listenerTerminals, contextResults);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
	}


}
