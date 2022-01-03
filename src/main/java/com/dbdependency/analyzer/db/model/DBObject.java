package com.dbdependency.analyzer.db.model;

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
public class DBObject {

	private String OBJECT_NAME;
	private String OBJECT_TYPE;
	private String OWNER;
}
