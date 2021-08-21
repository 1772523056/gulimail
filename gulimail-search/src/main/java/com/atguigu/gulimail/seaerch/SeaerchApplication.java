package com.atguigu.gulimail.seaerch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class SeaerchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeaerchApplication.class, args);
	}

}
