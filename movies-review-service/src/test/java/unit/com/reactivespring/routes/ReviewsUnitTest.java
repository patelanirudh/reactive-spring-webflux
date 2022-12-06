package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    private static final String MOVIE_REVIEW_URL = "/v1/reviews";

    @Test
    void test_addReview() {
        // given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        // when/then
        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

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
    void test_getAllReviews() {
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0),
                new Review(null, 2L, "Shitty Movie", 3.0));

        // when
        when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviewsList));

        // then
        webTestClient.get()
                .uri(MOVIE_REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(4);
    }

    @Test
    void test_deleteReview() {
        // given
        var reviewToDelete = "abc";
        var oldReview = new Review(null, 1L, "Awesome Movie", 9.0);

        // when
        when(reviewReactiveRepository.deleteById(isA(String.class))).thenReturn(Mono.empty());

        // then
        webTestClient.delete()
                .uri(MOVIE_REVIEW_URL + "/{id}", reviewToDelete)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}
