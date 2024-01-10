package ru.quinsis.sqltrainer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@EnableAsync
public class Demo6Application {
	public static void main(String[] args) {
		SpringApplication.run(Demo6Application.class, args);
	}
}
