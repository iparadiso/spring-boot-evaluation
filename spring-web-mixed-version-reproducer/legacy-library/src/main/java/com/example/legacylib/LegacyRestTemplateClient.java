package com.example.legacylib;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * An ordinary REST client of the kind published as a library, compiled once against Spring Boot 3
 * / Spring Framework 6.0.13 and consumed as a binary.
 *
 * <p><strong>This class is never recompiled.</strong> Recompiling it against Spring 7 makes the
 * problem disappear, which is why the reproducer builds it against Spring 6.
 *
 * <p>There are two pairs of methods below. Within each pair the two methods are identical except
 * for a single line: the type used to carry the headers. The {@link HttpHeaders} version fails on
 * Spring Boot 4; the {@link LinkedMultiValueMap} version works.
 */
public class LegacyRestTemplateClient {

	private final RestTemplate restTemplate;
	private final String url;

	public LegacyRestTemplateClient(String url) {
		this.url = url;
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(3_000);
		factory.setReadTimeout(3_000);
		this.restTemplate = new RestTemplate(factory);
	}

	// ==============================================================================
	// PAIR 1 -- GET with headers
	// ==============================================================================

	/**
	 * BREAKS on Spring Boot 4.
	 *
	 * <p>This is the textbook way to send headers, and it is the example in {@code HttpEntity}'s
	 * own Javadoc.
	 */
	public String getWithHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		return status(restTemplate.exchange(URI.create(url), HttpMethod.GET, entity, String.class));
	}

	/**
	 * WORKS on Spring Boot 4. Identical to {@link #getWithHttpHeaders()} except that the headers
	 * are carried in a {@link LinkedMultiValueMap} instead of an {@link HttpHeaders}.
	 */
	public String getWithLinkedMultiValueMap() {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		return status(restTemplate.exchange(URI.create(url), HttpMethod.GET, entity, String.class));
	}

	// ==============================================================================
	// PAIR 2 -- POST with a body and headers
	// ==============================================================================

	/** BREAKS on Spring Boot 4. */
	public String postWithHttpHeaders(String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>(body, headers);
		return status(restTemplate.exchange(URI.create(url), HttpMethod.POST, entity, String.class));
	}

	/**
	 * WORKS on Spring Boot 4. Identical to {@link #postWithHttpHeaders(String)} except for the
	 * headers type.
	 */
	public String postWithLinkedMultiValueMap(String body) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		HttpEntity<String> entity = new HttpEntity<>(body, headers);
		return status(restTemplate.exchange(URI.create(url), HttpMethod.POST, entity, String.class));
	}

	private String status(ResponseEntity<String> response) {
		return "status=" + response.getStatusCode().value();
	}
}
