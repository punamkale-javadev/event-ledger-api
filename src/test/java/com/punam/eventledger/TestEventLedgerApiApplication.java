package com.punam.eventledger;

import org.springframework.boot.SpringApplication;

public class TestEventLedgerApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(EventLedgerApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
