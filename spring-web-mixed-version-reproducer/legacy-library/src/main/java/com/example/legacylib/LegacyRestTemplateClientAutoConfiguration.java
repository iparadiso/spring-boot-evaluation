package com.example.legacylib;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link LegacyRestTemplateClient}, compiled against Spring Boot 3.0.13.
 *
 * <p>Registered through {@code META-INF/spring/org.springframework.boot.autoconfigure
 * .AutoConfiguration.imports}. That file name and the {@code @AutoConfiguration} annotation are
 * unchanged between Spring Boot 3 and 4, so a Spring Boot 4 application picks this up from the
 * library jar with no modification -- which is the point. This is an ordinary Spring Boot starter
 * behaving ordinarily; the application does nothing unusual to consume it.
 *
 * <p>The bean is deliberately built with a plain {@code new RestTemplate(...)} inside the client
 * rather than with {@code RestTemplateBuilder}. Boot's builder has its own 3-to-4 API differences,
 * and dragging them in would blur the question being asked. The only variable this reproducer
 * intends to isolate is {@code HttpHeaders} losing the {@code MultiValueMap} contract.
 */
@AutoConfiguration
public class LegacyRestTemplateClientAutoConfiguration {

	/**
	 * @param baseUrl the target the client calls. Defaults to a local address so that an
	 * application can start without configuring anything; the tests point it at a stub server.
	 */
	@Bean
	@ConditionalOnMissingBean
	public LegacyRestTemplateClient legacyRestTemplateClient(
			@Value("${legacy.client.base-url:http://localhost:8080/}") String baseUrl) {
		return new LegacyRestTemplateClient(baseUrl);
	}
}
