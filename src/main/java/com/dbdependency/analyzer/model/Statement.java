package com.dbdependency.analyzer.model;


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
public class Statement {

	private String listenerMethodName;
	private String statementWithBracket;
	private int line;
	private String pureStatement;

}
