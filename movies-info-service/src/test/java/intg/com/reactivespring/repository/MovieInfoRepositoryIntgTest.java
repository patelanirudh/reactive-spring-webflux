package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

// Scans repository classes and make them available. Full spring app. context is not required for database layer
// Only loads MongoDB configuration and components
@DataMongoTest
// This profile is not available so will not hinder with existing profile configs(app-files).
@ActiveProfiles("test")
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;


    @BeforeEach
    void setup() {
        // "id" is provided only in 3rd Movie entry
        List<MovieInfo> moviesList = List.of(new MovieInfo(null, "Batman Begins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")),
                new MovieInfo(null, "Valkyrie", 2008,
                        List.of("Tom Cruise", "Bill Nighty"), LocalDate.parse("2008-12-25")));
        // subscribe to flux and block until the last element completes, else we may run findAll before all data
        // is saved in the repo.
        movieInfoRepository.saveAll(moviesList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        // let entire data be deleted from repo thus block
        movieInfoRepository.deleteAll()
                .block();
    }


    @DisplayName("findAll")
    @Test
    void test_FindAll() {
        // given

        // when
        var movieInfoFlux = movieInfoRepository.findAll()
                .log();

        // then
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @DisplayName("findById")
    @Test
    void test_FindById() {
        // given
        // See above in @BeforeEach
        String movieIdToFind = "abc";

        // when
        var movieInfoFlux = movieInfoRepository.findById(movieIdToFind)
                .log();

        // then
        StepVerifier.create(movieInfoFlux)
                .assertNext(movie -> {
                    Assertions.assertEquals("Dark Knight Rises", movie.getName(), "MovieName should have matched");
                })
                .verifyComplete();
    }


    @DisplayName("saveMovieInfo")
    @Test
    void test_SaveMovieInfo() {
        // given
//        Mono<MovieInfo> singleMoviePublisher = Mono.just(new MovieInfo(null, "The Judge", 2014,
//                List.of("Robert Downey Jr", "Robert Duvall"), LocalDate.parse("2014-10-02")));

        // when
        var movieInfoMono = movieInfoRepository.save(new MovieInfo(null, "The Judge", 2014,
                        List.of("Robert Downey Jr", "Robert Duvall"), LocalDate.parse("2014-10-02")))
                .log();


        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movie -> {
                    Assertions.assertNotNull(movie.getMovieInfoId(), "MovieId should not be null");
                    Assertions.assertEquals("The Judge", movie.getName(), "MovieName should have matched");
                })
                .verifyComplete();
    }

    @DisplayName("updateMovieInfo")
    @Test
    void test_UpdateMovieInfo() {
        // given
        Mono<MovieInfo> movieFound = movieInfoRepository.findById("abc");
        movieFound = movieFound.flatMap(m -> {
                    m.setYear(2022);
                    return Mono.just(m);
                })
                .log();

        // when : using saveAll just to test Publisher
        var movieUpdatedInfo = movieInfoRepository.saveAll(movieFound) //.save(movieFound)
                .log();

        // then
        StepVerifier.create(movieUpdatedInfo)
                .assertNext(movie -> {
                    Assertions.assertEquals(2022, movie.getYear(), "MovieYear should have been updated");
                })
                .verifyComplete();
    }

    @DisplayName("deleteMovieInfo")
    @Test
    void test_DeleteMovieInfo() {
        // given

        // when
        movieInfoRepository.deleteById("abc")
                .block();
        var moviesFlux = movieInfoRepository.findAll().log();

        // then
        StepVerifier.create(moviesFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @DisplayName("findMovieInfoByYear")
    @Test
    void test_findMovieInfoByYear() {
        // given
        Integer movieYear = 2008;

        // when
        Flux<MovieInfo> moviesByYear = movieInfoRepository.findByYear(movieYear).log();

        // then
        StepVerifier.create(moviesByYear)
                .expectNextCount(2)
                .verifyComplete();
    }

    @DisplayName("findMovieInfoByName")
    @Test
    void test_findMovieInfoByName() {
        // given
        String movieNameToFind = "Dark Knight Rises";

        // when
        Mono<MovieInfo> moviesByName = movieInfoRepository.findByName(movieNameToFind).log();

        // then
        StepVerifier.create(moviesByName)
                .assertNext(movieInfo -> {
                    assert movieInfo != null;
                    Assertions.assertEquals(movieNameToFind, movieInfo.getName(), "Movie name should match");
                })
                .verifyComplete();
    }

}