package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler) {
        // Handler Function here is a lambda which takes a ServerRequest and return a ServerResponse
        return route()
                .nest(path("/v1/reviews"), builder -> {
                    builder
                            .GET("", request -> reviewHandler.getReviews(request))
                            .POST("", request -> reviewHandler.addReview(request))
                            .PUT("/{id}", request -> reviewHandler.updateReview(request))
                            .DELETE("/{id}",  request -> reviewHandler.deleteReview(request));
                })
                .GET("/v1/helloworld", request -> ServerResponse.ok().bodyValue("helloworld"))
                .build();

//        return route()
//                .GET("/v1/helloworld", (request -> ServerResponse.ok().bodyValue("helloworld")))
//                .POST("/v1/reviews", request -> reviewHandler.addReview(request))
//                .GET("/v1/reviews", request -> reviewHandler.getReviews(request))
//                .build();
    }
}
