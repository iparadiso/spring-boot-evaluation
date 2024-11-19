package com.example.demo;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppConfiguration {

    @Bean
    public Animal testAnimal(Environment environment) {
        if (environment.getProperty("new.cat-mode.enabled", Boolean.class, false)) {
            return new Cat();
        }
        return new Dog();
    }
}
