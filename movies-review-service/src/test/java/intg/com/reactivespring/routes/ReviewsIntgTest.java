package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// This profile is not available so will not hinder with existing profile configs(app-files) and will connect to
// locally running/embedded MongoDB instance
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    private static final String MOVIE_REVIEW_URL = "/v1/reviews";
    private Long totalRepoCount;

    @BeforeEach
    void setup() {
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0),
                new Review(null, 2L, "Shitty Movie", 3.0));

        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();
        totalRepoCount = reviewReactiveRepository.count().block();
    }

    @AfterEach
    void cleanUp() {
        System.out.println("!!!!!!!!!!!! @AfterAll RUNNING CLEANUP !!!!!!!!!!!");
        reviewReactiveRepository.deleteAll()
                .block();
    }

    @Test
    void test_addReview() {
        // given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        // when/then
        webTestClient.post()
                .uri(MOVIE_REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    Review savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    System.out.println("======= Added MovieInfo : " + savedReview + " =========");
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });
    }

    @Test
    void test_getAllMovieReviews() {
        // given

        // when/then
        Flux<Review> reviewsFluxResponse = webTestClient.get()
                .uri(MOVIE_REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Review.class)
                .getResponseBody();
                // also works
                //        .expectBodyList(Review.class)
                //                .value(reviews -> {
                //                    assertEquals(3, reviews.size());
                //                });
        StepVerifier.create(reviewsFluxResponse)
                .expectNextCount(totalRepoCount)
                .verifyComplete();
    }

    @Test
    void test_deleteMovieReview() {
        // given
        System.out.println("Total Repo Count : " + totalRepoCount);
        List<String> ids = getAllReviewIds();

        // when/then
        webTestClient.delete()
                .uri(MOVIE_REVIEW_URL + "/{id}", ids.get(0))
                .exchange()
                .expectStatus()
                .isNoContent();

        Long currentRepoCount = reviewReactiveRepository.count().block();

        System.out.println("Current Repo Count : " + currentRepoCount);
        Assertions.assertNotEquals(totalRepoCount, currentRepoCount);
    }

    @Test
    void test_getReviewsByeMovieInfoId() {
        // given
        // works
//        Long movieInfoId = 2L;
//        URI uriByMovieInfoId = UriComponentsBuilder.fromUriString(MOVIE_REVIEW_URL)
//                .queryParam("movieInfoId", movieInfoId)
//                .buildAndExpand()
//                .toUri();

        // when/then
        webTestClient.get()
                .uri(uriBuilder -> {
                    return uriBuilder.path(MOVIE_REVIEW_URL)
                            .queryParam("movieInfoId", "2")
                            .build();
                })
                // .uri(uriByMovieInfoId) this also works
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    private List<String> getAllReviewIds() {
        Flux<Review> reviewsFlux = reviewReactiveRepository.findAll();
        Mono<List<String>> listMono = reviewsFlux.map(review -> review.getReviewId())
                .collectList();
        List<String> ids = listMono.block();
        return ids;
    }

}
