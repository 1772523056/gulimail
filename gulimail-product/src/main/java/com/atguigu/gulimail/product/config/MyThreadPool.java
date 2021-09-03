package com.atguigu.gulimail.product.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
@Configuration
public class MyThreadPool {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties properties) {
        return new ThreadPoolExecutor(properties.getCoreSize(),
                properties.getMaxSize(),
                properties.getKeepAliveTime(),
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
