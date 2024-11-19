package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

public class PropertyEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE = "classpath:/custom-properties.properties";

    /**
     * Post-process the given {@code environment}.
     *
     * @param environment the environment to post-process
     * @param application the application to which the environment belongs
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.acceptsProfiles(Profiles.of("laptop"))) {
            try {
                environment.getPropertySources()
                        .addLast(new ResourcePropertySource("custom-prop", PROPERTY_SOURCE));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
