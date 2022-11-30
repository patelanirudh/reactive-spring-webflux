package com.reactivespring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class FluxAndMonoController {

    @GetMapping(path = "/flux")
    public Flux<Integer> flux() {
        return Flux.just(1, 2, 3, 4, 5, 6);
    }
}
