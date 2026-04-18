package com.cventro.infaService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class InfaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InfaServiceApplication.class, args);
	}

}
