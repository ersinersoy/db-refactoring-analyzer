package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dbdependency.analyzer.service.DBService;

@SpringBootTest
class DemoApplicationTests {


@Autowired
DBService c;

	@Test
	void contextLoads() {
		
	}

}
