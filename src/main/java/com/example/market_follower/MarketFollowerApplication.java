package com.example.market_follower;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarketFollowerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketFollowerApplication.class, args);
	}

}
