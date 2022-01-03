package com.dbdependency.analyzer.model.result;


import com.dbdependency.analyzer.model.Statement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DependencyResult {

	private int line;
	private String operation; // SELECT, INSERT, UPDATE, DELETE, CURSOR, EXECUTE IMMEDIATE
	private String column;
	private String table;
	private String owner;
	private Statement statement;
	private boolean multibleAssignmentExist =false;
	private boolean complexAssignmentExist =false;
	private boolean stringConcatenationExist = false;
	private boolean tableNotCatched = false;
	private boolean isException = false;

}
