package com.example.library;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class BarAutoConfiguration {
    
    @Bean
    public Bar bar() {
        return new Bar();
    }
}