package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    private static String MOVIE_INFO_URL = "/v1/movieinfos";

    @MockBean
    private MovieInfoService movieInfoServiceMock;

    @DisplayName("GetAllMovieInfos")
    @Test
    void test_getAllMovieInfos() {
        // given
        var movies = List.of(new MovieInfo(null, "Batman Begins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        // when : mock service getAllMovieInfos()
        when(movieInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movies));

        // then
        webTestClient.get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @DisplayName("GetAllMovieInfosByYear")
    @Test
    void test_getAllMovieInfosByYear() {
        // given
        Integer yearToFind = 2005;
        var movies = List.of(new MovieInfo(null, "Batman Begins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        // when : mock service getMovieInfoByYear() to return just 1 movie match based on given year
        when(movieInfoServiceMock.getMovieInfoByYear(isA(Integer.class))).thenReturn(Flux.just(movies.get(0)));

        // then

        URI uriByYear = UriComponentsBuilder.fromUriString(MOVIE_INFO_URL)
                .queryParam("year", yearToFind)
                .buildAndExpand()
                .toUri();

        webTestClient.get()
                .uri(uriByYear)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @DisplayName("GetMovieInfoById")
    @Test
    void test_getMovieInfoById() {
        // given
        String movieId = "abc";
        var movie = Mono.just(new MovieInfo(movieId, "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        // when : mock service getAllMovieInfos()
        // mockito : isA and any can be used
        when(movieInfoServiceMock.getMovieInfoById(anyString())).thenReturn(movie);

        // then
        webTestClient.get()
                .uri(MOVIE_INFO_URL + "/{id}", movieId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfoRecord = movieInfoEntityExchangeResult.getResponseBody();
                    assert movieInfoRecord != null;
                    assertTrue(movieInfoRecord.getName().equalsIgnoreCase("Dark Knight Rises"), "Movie name sshould match");
                });
    }

    @DisplayName("POSTMovieInfo")
    @Test
    void test_addMovieInfo() {
        // given
        var movie = new MovieInfo(null, "Batman Begins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        // when : mock service addMovieInfo(MovieInfo) and returned "mockId"
        // mockito : isA and any can be used
        when(movieInfoServiceMock.addMovieInfo(any(MovieInfo.class))).thenReturn(Mono.just(new MovieInfo("mockId", "Batman Begins", 2005,
                List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        // then
        webTestClient.post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo savedMoviedInfo = movieInfoEntityExchangeResult.getResponseBody();
                    System.out.println("======= Added MovieInfo : " + savedMoviedInfo + " =========");
                    assert savedMoviedInfo != null;
                    assertEquals("mockId",savedMoviedInfo.getMovieInfoId(), "MovieInfo id should match");
                });
    }

    @DisplayName("PUTMovieInfoById")
    @Test
    void test_updateMovieInfoById() {
        // given
        String movieId = "abc";
        var movie = new MovieInfo(movieId, "Batman Begins", 2005,
                List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        // when : mock service addMovieInfo(MovieInfo) and returned "mockId"
        // mockito : isA and any can be used
        when(movieInfoServiceMock.updateMovieInfoById(any(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.just(new MovieInfo("abc", "Batman Begins333", 2007,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        // then
        webTestClient.put()
                .uri(MOVIE_INFO_URL + "/{movieId}", movieId)
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo updatedMoviedInfo = movieInfoEntityExchangeResult.getResponseBody();
                    System.out.println("======= Updated MovieInfo : " + updatedMoviedInfo + " =========");
                    assert updatedMoviedInfo != null;
                    assertEquals("Batman Begins333",updatedMoviedInfo.getName(), "MovieInfo name should match");
                    assertEquals(2007, updatedMoviedInfo.getYear(), "MovieInfo year should match");
                });
    }

    @DisplayName("DeleteMovieInfoById")
    @Test
    void test_deleteMovieInfoById() {
        // given
        String movieId = "abc";
         Mono<Void> monoVoid = Mono.empty(); // we can use this directly as well in thenReturn

        // when : mock service addMovieInfo(MovieInfo) and returned "mockId"
        // mockito : isA and any can be used
        when(movieInfoServiceMock.deleteMovieInfoById(isA(String.class))).thenReturn(monoVoid);

        // then
        webTestClient
                .delete()
                .uri(MOVIE_INFO_URL + "/{movieId}", movieId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @DisplayName("POSTMovieInfoInvalidParams")
    @Test
    void test_addMovieInfo_InvalidParams() {
        // given : empty name, cast and negative year
        var movie = new MovieInfo(null, "", -2005,
                List.of(""), LocalDate.parse("2005-06-15"));

        // when : mock is not needed since the call with not go service layer and error out before

        // then
        webTestClient.post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    var expectedErrorMsg = "MovieInfo.cast should be present,MovieInfo.name should be present,MovieInfo.year must be a Positive value";
                    assert responseBody != null;
                    assertEquals(expectedErrorMsg, responseBody);
                });
    }

    @DisplayName("GetMovieInfoById_NotFound")
    @Test
    void test_getMovieInfoById_NotFound() {
        // given
        String movieId = "abcxxx";

        // when : mock to return nothing
        when(movieInfoServiceMock.getMovieInfoById(isA(String.class))).thenReturn(Mono.empty());

        // then
        webTestClient.get()
                .uri(MOVIE_INFO_URL + "/{movieId}", movieId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }


}
