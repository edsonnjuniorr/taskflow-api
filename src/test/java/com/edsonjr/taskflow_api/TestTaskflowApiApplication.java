package com.edsonjr.taskflow_api;

import org.springframework.boot.SpringApplication;

public class TestTaskflowApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(TaskflowApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
