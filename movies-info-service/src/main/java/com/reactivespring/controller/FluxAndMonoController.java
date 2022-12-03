package com.reactivespring.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class FluxAndMonoController {
// Create a Test : Ctrl + Shift + T

    @GetMapping(path = "/flux")
    public Flux<Integer> flux() {
        return Flux.just(1, 2, 3, 4, 5, 6)
                .log();
    }

    // produce stream of data to the client
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> stream() {
        return Flux.interval(Duration.ofSeconds(1))
                .log();
    }

    @GetMapping(value = "/mono")
    public Mono<String> mono() {
        return Mono.just("Hello World Mono")
                .log();
    }
}
