package com.atguigu.gulimail.product.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {
    @Bean
    RedissonClient redisClient(){
        return Redisson.create();
    }

}
