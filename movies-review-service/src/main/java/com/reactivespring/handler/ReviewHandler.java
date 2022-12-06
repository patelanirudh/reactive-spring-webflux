package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    private ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        log.info("!!! ReviewHandler addReview invoked !!!");

        return request.bodyToMono(Review.class)
                .flatMap(reviewReactiveRepository::save) // save it (reqReview -> reviewReactiveRepository.save(reqReview))
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview)); // return response
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        log.info("!!! ReviewHandler getReviews invoked !!!");

        Optional<String> movieInfoId = request.queryParam("movieInfoId");
        if (movieInfoId.isPresent()) {
            log.info("!!! ReviewHandler getReviews by movieInfoId {} ", movieInfoId.get());
            Flux<Review> movieInfoReviewFlux = reviewReactiveRepository.findByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return buildReviewsResponse(movieInfoReviewFlux);
        } else {
            Flux<Review> reviewFlux = reviewReactiveRepository.findAll().log();
            return buildReviewsResponse(reviewFlux);
        }
    }

    private static Mono<ServerResponse> buildReviewsResponse(Flux<Review> movieInfoReviewFlux) {
        return ServerResponse.ok().body(movieInfoReviewFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        log.info("!!! ReviewHandler updateReview invoked !!!");

        String reviewId = request.pathVariable("id");
        Mono<Review> existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class) // 'review' == 'existingReview'
                        .map(reqReview -> {// this map is done on Review just got from request by bodyToMono and not on existingReview
                            // 'reqReview' == bodyToMono reqReview
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(updatedReview -> reviewReactiveRepository.save(updatedReview)) // from above updatedReview
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        String reviewIdToDelete = request.pathVariable("id");
        log.info("!!! ReviewHandler deleteReview invoked, reviewIdToDelete {} ", reviewIdToDelete);

        return reviewReactiveRepository.deleteById(reviewIdToDelete)
                .then(ServerResponse.noContent().build());
    }

    Mono<List<String>> getAllReviewIds() {
          return reviewReactiveRepository.findAll()
                      .distinct(Review::getReviewId)
                 .map(Review::getReviewId)
                  .collect(Collectors.toList());
    }
}
