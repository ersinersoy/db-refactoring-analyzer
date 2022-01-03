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
public class Column {

	private String name;
	private int line;
	private boolean isMultibleTableViewExist = false;
	private Map<String,TableView> tableView;

}
