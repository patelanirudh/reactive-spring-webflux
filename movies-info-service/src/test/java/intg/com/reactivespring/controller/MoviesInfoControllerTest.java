package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// This profile is not available so will not hinder with existing profile configs(app-files) and will connect to
// locally running/embedded MongoDB instance
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MoviesInfoControllerTest {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    private final String customMovieInfoId = "abc";
    private static String MOVIE_INFO_URL = "/v1/movieinfos";
    private Long movieInfoRepoCount;

    @BeforeAll
    void setUp() {
        System.out.println("!!!!!!!!!!!! @BeforeAll RUNNING SETUP : MovieInfo Repo Count " + movieInfoRepository.count().block() + " !!!!!!!!!!!");

        // "id" is provided only in 3rd Movie entry
        List<MovieInfo> moviesList = List.of(new MovieInfo(null, "Batman Begins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo(customMovieInfoId, "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        // subscribe to flux and block until the last element completes, else we may run findAll before all data
        // is saved in the repo.
        movieInfoRepository.saveAll(moviesList)
                .blockLast();
    }

//    @AfterEach
//    void tearDown() {
//         let entire data be deleted from repo thus block
//        movieInfoRepository.deleteAll()
//                .block();
//    }

    @AfterAll
    void cleanUp() {
        System.out.println("!!!!!!!!!!!! @AfterAll RUNNING CLEANUP !!!!!!!!!!!");
        movieInfoRepository.deleteAll()
                .block();
    }

    @Order(1)
    @DisplayName("POST MovieInfo")
    @Test
    void addMovieInfo() {
        // null as Id is passed so that it gets auto-generated
        var movieInfo = new MovieInfo(null, "Dark Knight Rises111",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        // when
        webTestClient.post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo savedMoviedInfo = movieInfoEntityExchangeResult.getResponseBody();
                    System.out.println("======= Added MovieInfo : " + savedMoviedInfo + " =========");
                    assert savedMoviedInfo != null;
                    assert savedMoviedInfo.getMovieInfoId() != null;
                });

        movieInfoRepoCount = movieInfoRepository.count().block();
        System.out.println("!!!!!!!! POST MovieInfo Repo Count : " + movieInfoRepoCount + " !!!!!!!!!!!");
    }

    @Order(2)
    @DisplayName("GET AllMovieInfos")
    @Test
    void getAllMovies() {
        // given
        System.out.println("!!!!!!!! GET MovieInfo Repo Count : " + movieInfoRepoCount + " !!!!!!!!!");

        // when
        var movies = webTestClient.get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        // then
        StepVerifier.create(movies)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Order(3)
    @DisplayName("GET MovieInfoById")
    @Test
    void getMovieInfoById() {
        // given
        System.out.println("!!!!!!!! GET MovieInfo Repo Count : " + movieInfoRepoCount + " !!!!!!!!!");
        String movieId = "abc";

        // when : both ways work to validate data
        webTestClient.get()
                .uri(MOVIE_INFO_URL + "/{id}", movieId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
//                .expectBody(MovieInfo.class)
//                .consumeWith(movieInfoEntityExchangeResult -> {
//                    var movieInfoRecord = movieInfoEntityExchangeResult.getResponseBody();
//                    assert movieInfoRecord != null;
//                    assertTrue(movieInfoRecord.getMovieInfoId().equalsIgnoreCase(movieId), "MovieId should match");
//                });

        // then
    }

    @Order(4)
    @DisplayName("Put UpdateMovieInfoById")
    @Test
    void updateMovieInfoById() {
        // given
        String movieIdToUpdate = "abc";
        // sending Id null as we are separately passing it as Path Param
        MovieInfo newMovie = new MovieInfo(null, "Dark Knight Rises Forever",
                2022, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        // when
        webTestClient.put()
                .uri(MOVIE_INFO_URL + "/{movieId}", movieIdToUpdate)
                .bodyValue(newMovie)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    Assertions.assertEquals("Dark Knight Rises Forever", responseBody.getName(), "Movie name should match");
                    assert 2022 == responseBody.getYear();
                });
        // then
    }

    @Order(5)
    @DisplayName("Delete DeleteMovieInfoById")
    @Test
    void deleteMovieInfoById() {
        // given
        String movieIdToDelete = "abc";

        // when
        webTestClient.delete()
                .uri(MOVIE_INFO_URL + "/{movieId}", movieIdToDelete)
                .exchange()
                .expectStatus()
                .isNoContent();
        // then
    }
}