package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AppConfigurationTest {

    private final SpringApplication application = new SpringApplication();

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AppConfiguration.class))
            .withInitializer(
                    ac -> new PropertyEnvironmentPostProcessor().postProcessEnvironment(ac.getEnvironment(), application));

    @Test
    void testCatMode() {
        contextRunner
                .withSystemProperties("spring.profiles.active=laptop",
                        "spring.application.name=apptest")
                .run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(Cat.class);
        });
    }

    @Test
    void testDefaultMode() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(Dog.class);
                });
    }
}
