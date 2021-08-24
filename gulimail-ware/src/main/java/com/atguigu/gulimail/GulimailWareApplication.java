package com.atguigu.gulimail;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@MapperScan("com.atguigu.gulimail.ware.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimailWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailWareApplication.class, args);
    }

}
