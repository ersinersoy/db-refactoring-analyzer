package com.dbdependency.analyzer.model;


import java.util.Map;

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
public class TableView {

	private String name;
	private int line;
	private boolean isMultibleSchemaExist = false;
	private Map<String,Schema> schema;
	private String type;

}
