package com.example.demo;

import com.example.library.Bar;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(AppConfig.class)
@AutoConfigureAfter(name = "com.example.library.BarAutoConfiguration")
@ConditionalOnClass(Bar.class)
@ConditionalOnBean(Bar.class)
public class FooConfig {

    @Bean
    public Foo foo(Bar bar) {
        return new Foo(bar);
    }

}
