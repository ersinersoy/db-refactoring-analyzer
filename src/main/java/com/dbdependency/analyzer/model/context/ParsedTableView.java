package com.dbdependency.analyzer.model.context;


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
public class ParsedTableView {

	private String alias;
	private String owner;
	private String name;

}
