package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@Validated
@RequestMapping("/v1")
@Slf4j
public class MoviesInfoController {

    private MovieInfoService movieInfoService;

    // Ctor Dependency Injection
    public MoviesInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@Valid @RequestBody MovieInfo movieInfo) {
        log.info("!!!!!!! POST addMovieInfo entered !!!!!!!");
        return movieInfoService.addMovieInfo(movieInfo).log();
    }

    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(name = "year", required = false) Integer year) {
        log.info("!!!!!!! GET  getAllMovies entered, Year is {} !!!!!!!", year);
        if (null != year) {
            // See : This exception is propagated back to GlobalErrorHandler
            return movieInfoService.getMovieInfoByYear(year)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No movie for the given year is available")))
                    .log();
        }
        return movieInfoService.getAllMovieInfos()
                .log();
    }

    @GetMapping("/movieinfos/{movieId}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable(name = "movieId", required = true) String movieId) {
        log.info("!!!!!!! GET  getMovieInfoById entered !!!!!!!");
        return movieInfoService.getMovieInfoById(movieId)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @PutMapping("/movieinfos/{movieId}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfoById(@Valid @RequestBody MovieInfo movieInfo, @PathVariable(name = "movieId", required = true) String movieId) {
        log.info("!!!!!!! GET  updateMovieInfoById entered !!!!!!!");
        return movieInfoService.updateMovieInfoById(movieInfo, movieId)
                .map(movieInfo1 -> {
                    return ResponseEntity.ok().body(movieInfo1);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("movieinfos/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfoById(@PathVariable(value = "movieId", required = true) String movieId) {
        log.info("!!!!!!! Delete  deleteMovieInfoById entered !!!!!!!");
        return movieInfoService.deleteMovieInfoById(movieId);
    }
}
