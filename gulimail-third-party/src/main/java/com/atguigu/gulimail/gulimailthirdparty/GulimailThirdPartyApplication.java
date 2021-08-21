package com.atguigu.gulimail.gulimailthirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GulimailThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailThirdPartyApplication.class, args);
    }

}
