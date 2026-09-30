package com.example.spring_web_mixed_version_reproducer;

import com.example.legacylib.LegacyRestTemplateClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Calls a Spring Boot 3 library from a Spring Boot 4 application.
 *
 * <p>All four tests assert the same thing: the call returns HTTP 200. Two of them do. The two
 * that use {@code HttpHeaders} blow up with {@code IncompatibleClassChangeError} inside Spring's
 * own {@code RestTemplate}.
 *
 * <p><strong>This suite is expected to fail.</strong> That is the report.
 */
@SpringBootTest
@DisplayName("Spring Boot 3 library on a Spring Boot 4 runtime")
class LegacyLibraryCompatibilityTest {

	private static final StubServer SERVER = StubServer.start();

	@DynamicPropertySource
	static void registerBaseUrl(DynamicPropertyRegistry registry) {
		registry.add("legacy.client.base-url", SERVER::baseUrl);
	}

	@AfterAll
	static void stopServer() {
		SERVER.close();
	}

	/** Contributed by the library's own auto-configuration. */
	@Autowired
	private LegacyRestTemplateClient client;

	@Test
	@DisplayName("GET with HttpHeaders")
	void getWithHttpHeaders() {
		assertEquals("status=200", client.getWithHttpHeaders());
	}

	@Test
	@DisplayName("GET with LinkedMultiValueMap")
	void getWithLinkedMultiValueMap() {
		assertEquals("status=200", client.getWithLinkedMultiValueMap());
	}

	@Test
	@DisplayName("POST with HttpHeaders")
	void postWithHttpHeaders() {
		assertEquals("status=200", client.postWithHttpHeaders("{}"));
	}

	@Test
	@DisplayName("POST with LinkedMultiValueMap")
	void postWithLinkedMultiValueMap() {
		assertEquals("status=200", client.postWithLinkedMultiValueMap("{}"));
	}
}
