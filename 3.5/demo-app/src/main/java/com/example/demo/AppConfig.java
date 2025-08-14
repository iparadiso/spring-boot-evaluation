package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class AppConfig {

    @Bean
    public ConsumerOfFoo consumerOfFoo(List<Foo> foo) {
        return new ConsumerOfFoo(foo);
    }
}
