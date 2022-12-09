package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReviewsRestClient {

    private WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    // queryParam : movieInfoId
    public Flux<Review> retrieveReviews(String movieId) {
        var url = UriComponentsBuilder
                .fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUriString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status Code is : {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.empty();
                    }
                    // else for other cases do this
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsClientException(responseMessage)));

                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    // this will not get hit if remote service is completely down/unavailable. Only if remote service gives a 5xx error
                    // then that will come here and we can handle it
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsServerException("Server exception in ReviewService : "
                                    + responseMessage)));

                })
                .bodyToFlux(Review.class)
                .log();
    }
}
