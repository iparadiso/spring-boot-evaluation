package com.example.spring_web_mixed_version_reproducer;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * A stub HTTP server built on the JDK's own {@code com.sun.net.httpserver}.
 *
 * <p>Deliberately not MockWebServer, WireMock or {@code MockRestServiceServer}: this reproducer
 * should need nothing beyond Spring Boot itself, and it must not touch the network, so that
 * nothing outside the JDK and Spring can be blamed for the result.
 *
 * <p>Most of the failures here occur in {@code RestTemplate}'s request callback, which runs before
 * any bytes are written, so they would reproduce with no server at all. The server exists so the
 * control cases can return a real 200 and demonstrate that the transport is healthy.
 */
final class StubServer implements AutoCloseable {

	static final String RESPONSE_BODY = "{\"ok\":true}";

	private final HttpServer server;

	private StubServer(HttpServer server) {
		this.server = server;
	}

	/**
	 * Throws {@link UncheckedIOException} so that callers can start the server in a static field
	 * initializer, which must happen before {@code @DynamicPropertySource} publishes the base URL.
	 */
	static StubServer start() {
		try {
			return doStart();
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Could not start the stub HTTP server", ex);
		}
	}

	private static StubServer doStart() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.setExecutor(Executors.newFixedThreadPool(2));
		server.createContext("/", exchange -> {
			// Drain the request body; leaving it unread can stall the client connection.
			exchange.getRequestBody().readAllBytes();
			byte[] body = RESPONSE_BODY.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(body);
			}
		});
		server.start();
		return new StubServer(server);
	}

	String baseUrl() {
		return "http://127.0.0.1:" + server.getAddress().getPort() + "/";
	}

	@Override
	public void close() {
		server.stop(0);
	}
}
