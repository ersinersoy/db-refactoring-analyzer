package com.dbdependency.analyzer.model;

import java.util.List;
import java.util.Map;

import com.dbdependency.analyzer.model.result.DependencyResult;

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
public class Procedure {

	private String name;
	private Schema schema;
	private List<Assignment> assignment;
	private Map<String,Variable> variable;
	private List<DependencyResult> aDependencyResultList;

}
