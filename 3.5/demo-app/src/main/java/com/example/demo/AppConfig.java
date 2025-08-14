package com.example.demo;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
public class AppConfig {

    @Bean
    public ConsumerOfFoo consumerOfFoo(List<Foo> foo) {
        return new ConsumerOfFoo(foo);
    }
}
