package com.example.demo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean("foo")
    @Primary
    @ConditionalOnProperty(name = "foo.truthy", havingValue = "true", matchIfMissing = true)
    Boolean fooIsTrue() {
        return true;
    }

    @Bean("foo")
    Boolean fooIsFalse() {
        return false;
    }
}
