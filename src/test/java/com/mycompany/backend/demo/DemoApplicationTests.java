package com.mycompany.backend.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DemoApplicationTests {

	@Test
	void contextLoads() {
		assertDoesNotThrow(DemoApplication::new);
	}
}
