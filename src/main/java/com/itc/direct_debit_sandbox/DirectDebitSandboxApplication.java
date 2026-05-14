package com.itc.direct_debit_sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DirectDebitSandboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(DirectDebitSandboxApplication.class, args);
	}

}
