package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MoviesInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        var url = moviesInfoUrl.concat("/{movieId}");
        return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status Code is : {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new MoviesInfoClientException("No movieInfo available for the passed in Id :" + movieId,
                                clientResponse.statusCode().value()));
                    }
                    // else for other cases do this
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(responseMessage,
                                    clientResponse.statusCode().value())));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    // this will not get hit if remote service is completely down/unavailable. Only if remote service gives a 5xx error
                    // then that will come here and we can handle it
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException("Server exception in MovieInfoService : "
                                    + responseMessage)));

                })
                .bodyToMono(MovieInfo.class)
                .log();

    }
}
