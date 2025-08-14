package com.example.demo;

import com.example.library.Bar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = AppConfig.class, afterName = "com.example.library.BarAutoConfiguration")
@ConditionalOnClass(Bar.class)
@ConditionalOnBean(Bar.class)
public class FooAutoConfiguration {

    @Bean
    public Foo foo(Bar bar) {
        return new Foo(bar);
    }

}
