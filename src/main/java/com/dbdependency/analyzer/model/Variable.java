package com.dbdependency.analyzer.model;

import java.util.List;

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
public class Variable {

	private Statement statement;
	private int line;
	private String name;
	private boolean isMultiValueExist = false;
	private List<Assignment> assignments;
}
