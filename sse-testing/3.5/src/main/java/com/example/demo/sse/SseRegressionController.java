package com.example.demo.sse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reproducer for the Spring Framework gh-36385 regression introduced in 7.0.6 (SBN 4).
 *
 * SseEmitter.send() writes to response.getBody() and calls flush() on that stream.
 * Spring 7.0.6+ wraps the response body in a non-flushing decorator by default, so
 * those flush() calls are silently dropped and events never reach the client.
 *
 * Fix: add src/main/resources/spring.properties containing:
 *   spring.http.response.flush.enabled=true
 *
 * Note: application.properties / application.yml do NOT work — SpringProperties is
 * read at class-load time, before Spring's Environment is built.
 */
@RestController
class SseRegressionController {

    @GetMapping(value = "/sse/counter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter counter() throws IOException {
        var emitter = new SseEmitter(30_000L);
        var executor = Executors.newSingleThreadScheduledExecutor();
        var count = new AtomicInteger();

        emitter.send(SseEmitter.event().comment("connected"));

        executor.scheduleAtFixedRate(() -> {
            try {
                int tick = count.incrementAndGet();
                emitter.send(SseEmitter.event().name("tick").data("count=" + tick));
                if (tick >= 10) {
                    emitter.complete();
                    executor.shutdown();
                }
            } catch (IOException | IllegalStateException e) {
                emitter.completeWithError(e);
                executor.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);

        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(executor::shutdown);

        return emitter;
    }
}
