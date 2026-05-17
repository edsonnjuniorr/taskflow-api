package com.edsonjr.taskflow;

import org.springframework.boot.SpringApplication;

public class TestTaskflowApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(TaskflowApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
