package com.reactivespring.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;


@WebFluxTest(controllers = FluxAndMonoController.class)
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @DisplayName("flux_approach1")
    @Test
    void flux() {
        // given

        // when/assert
        webTestClient.get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Integer.class)
                .hasSize(6);

    }

    @DisplayName("flux_approach2")
    @Test
    void flux_approach2() {
        // given

        // when/assert
        var fluxReceived = webTestClient.get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(fluxReceived)
                .expectNext(1, 2, 3, 4, 5, 6)
                .verifyComplete();
    }

    @DisplayName("flux_approach3")
    @Test
    void flux_approach3() {
        // given

        // when/assert
        var fluxReceived = webTestClient.get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Integer.class)
                .consumeWith(listEntityExchangeResult -> {
                    var responseBody = listEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(responseBody, "Response body should not be null");
                    Assertions.assertEquals(6, responseBody.size());
                    // assert (Objects.requireNonNull(responseBody).size() == 6); this also works
                });

    }

    @DisplayName("mono_approach")
    @Test
    void mono_approach() {
        // given

        // when/assert
        var fluxReceived = webTestClient.get()
                .uri("/mono")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class) // as this is a Mono so expectBodyList is not required(as for Flux)
                .consumeWith(listEntityExchangeResult -> {
                    var responseBody = listEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(responseBody, "Response body should not be null");
                    Assertions.assertEquals("Hello World Mono", responseBody, "Returned body message does not match");
                    // assert (Objects.requireNonNull(responseBody).size() == 6); this also works
                });
    }

    @DisplayName("flux_stream_approach")
    @Test
    void stream_approach() {
        // given

        // when/assert
        var streamFluxReceived = webTestClient.get()
                .uri("/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(streamFluxReceived)
                .expectNext(0L, 1L, 2L)
                .thenCancel()// since we do not need to go on...
                .verify();

    }
}