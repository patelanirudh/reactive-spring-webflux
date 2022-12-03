package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
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
    public Mono<MovieInfo> addMovieInfo(@RequestBody MovieInfo movieInfo) {
        log.info("!!!!!!! POST addMovieInfo entered !!!!!!!");
        return movieInfoService.addMovieInfo(movieInfo).log();
    }

    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMovieInfos() {
        log.info("!!!!!!! GET  getAllMovies entered !!!!!!!");
        return movieInfoService.getAllMovieInfos().log();
    }

    @GetMapping("/movieinfos/{movieId}")
    public Mono<MovieInfo> getMovieInfoById(@PathVariable(name = "movieId", required = true) String movieId) {
        log.info("!!!!!!! GET  getMovieInfoById entered !!!!!!!");
        return movieInfoService.getMovieInfoById(movieId).log();
    }

    @PutMapping("/movieinfos/{movieId}")
    public Mono<MovieInfo> updateMovieInfoById(@RequestBody MovieInfo movieInfo, @PathVariable(name = "movieId", required = true) String movieId) {
        log.info("!!!!!!! GET  updateMovieInfoById entered !!!!!!!");
        return movieInfoService.updateMovieInfoById(movieInfo, movieId).log();
    }

    @DeleteMapping("movieinfos/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfoById(@PathVariable(value = "movieId", required = true) String movieId) {
        log.info("!!!!!!! Delete  deleteMovieInfoById entered !!!!!!!");
        return movieInfoService.deleteMovieInfoById(movieId);
    }
}
