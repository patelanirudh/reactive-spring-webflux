package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public static void main(String[] args) {
        // Class instantiation
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

        fluxAndMonoGeneratorService.namesFlux()
                .subscribe(name -> System.out.println("Flux Name is : " + name));

        System.out.print("Mono name is : ");
        fluxAndMonoGeneratorService.nameMono()
                .subscribe(System.out::println); // (p) -> method(p)

        fluxAndMonoGeneratorService.namesFlux_Map()
                .subscribe(name -> System.out.println("Mapped Flux Name is : " + name));

    }

    Mono<String> nameMono() {
        return Mono.just("Hello Mono").log();
    }

    Flux<String> namesFlux() {
        // this can come from db source or any other external/remote service
        return Flux.fromIterable(List.of("ANIRUDH", "SHILPI", "SHWETA", "MANU"))
                .log();
    }

    Flux<String> namesFlux_immutability() {
        // reactive streams are immutable : to see the changes we have to chain the calls
        Flux<String> namesFlux = Flux.fromIterable(List.of("ANIRUDH", "SHILPI", "SHWETA", "MANU"));
        namesFlux.map(String::toLowerCase); // this returns new Flux
        return namesFlux.log();

    }

    Mono<String> namesMono_map(int stringLength) {
        return Mono.just("anirudh")
                .map(String::toUpperCase)
                .filter(name -> name.length() >= stringLength)
                .log();
    }

    Flux<String> namesFlux_Map() {
        // this can come from db source or any other external/remote service
        return Flux.fromIterable(List.of("ANIRUDH", "SHILPI", "SHWETA", "MANU"))
//                .map(name -> name.toUpperCase())
                .map(String::toLowerCase)
                .log();
    }

    Flux<String> namesFlux_Filter(int stringLength) {
        // this can come from db source or any other external/remote service
        return Flux.fromIterable(List.of("ANIRUDH", "SHILPI", "SHWETA", "MANU"))
                .filter(name -> (name.length() >= stringLength))
                .map(String::toLowerCase)
                .map(name -> name.length() + "-" + name)
                .log();
    }

    // Mono uses flatMap for transforming to List<String>
    Mono<List<String>> namesMono_flatMap(int stringLength) {
        return Mono.just("anirudh")
                .map(String::toUpperCase)
                .filter(name -> name.length() >= stringLength)
                .flatMap(FluxAndMonoGeneratorService::splitStringMono)
                .log();
    }

    // Mono uses flatMapMany for transforming : if returning Flux from Mono pipeline
    Flux<String> namesMono_flatMapMany(int stringLength) {
        return Mono.just("anirudh")
                .map(String::toUpperCase)
                .filter(name -> name.length() >= stringLength)
                .flatMapMany(FluxAndMonoGeneratorService::splitString)
                .log();
    }

    Flux<String> namesFlux_FlatMap(int stringLength) {
        // this can come from db source or any other external/remote service
        // flatMap returns Publisher after transformation
        return Flux.fromIterable(List.of("ANIRUDH", "SHILPII", "SHWETA", "MANU"))
                .filter(name -> (name.length() >= stringLength))
                .map(String::toLowerCase)
                .flatMap(FluxAndMonoGeneratorService::splitString) // s -> FluxAndMonoGeneratorService.splitString(s)
                .doOnNext(System.out::println)
                .log();
    }

    Flux<String> namesFlux_FlatMap_Async(int stringLength) {
        // this can come from db source or any other external/remote service
        // flatMap is not good if ordering is required
        return Flux.fromIterable(List.of("ANIRUDH", "SHILPII", "SHWETA", "MANU"))
                .filter(name -> (name.length() >= stringLength))
                .map(String::toLowerCase)
                .flatMap(FluxAndMonoGeneratorService::splitString_withDelay) // s -> FluxAndMonoGeneratorService.splitString(s)
//                .doOnNext(System.out::println)
                .log();
    }

    Flux<String> namesFlux_ConcatMap_Async(int stringLength) {
        // this can come from db source or any other external/remote service
        // ConcatMap is good if ordering is required
        return Flux.fromIterable(List.of("ANIRUDH", "SHILPII", "SHWETA", "MANU"))
                .filter(name -> (name.length() >= stringLength))
                .map(String::toLowerCase)
                .concatMap(FluxAndMonoGeneratorService::splitString_withDelay)
//                .flatMap(FluxAndMonoGeneratorService::splitString_withDelay) // s -> FluxAndMonoGeneratorService.splitString(s)
//                .doOnNext(System.out::println)
                .log();
    }

    Flux<String> namesFlux_Transform(int stringLength) {
        // Transform : helps in extracting the common functionality and use it wherever needed
        // Function<Input, Return>
        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toLowerCase) // chain the flux and then return
                .filter(s -> s.length() >= stringLength);

        // this can come from db source or any other external/remote service
        // flatMap returns Publisher after transformation
        return Flux.fromIterable(List.of("ANIRUDH", "SHILPII", "SHWETA", "MANU"))
                .transform(filterMap)
                .flatMap(FluxAndMonoGeneratorService::splitString) // s -> FluxAndMonoGeneratorService.splitString(s)
                .defaultIfEmpty("default") // if after transform and flatMap we get nothing then return default
//                .doOnNext(System.out::println)
                .log();
    }

    Flux<String> namesFlux_Transform_SwitchIfEmpty(int stringLength) {
        // Transform : helps in extracting the common functionality and use it wherever needed
        // Function<Input, Return>
        Function<Flux<String>, Flux<String>> filterMap = name ->
                name.map(String::toLowerCase) // chain the flux and then return
                        .filter(s -> s.length() >= stringLength)
                        .flatMap(FluxAndMonoGeneratorService::splitString);

        // This is the defaultFlux to return. This is done so method code becomes common when there is nothing to return.
        var defaultFlux = Flux.just("DEFAULTT")
                .transform(filterMap);

        // this can come from db source or any other external/remote service
        // flatMap returns Publisher after transformation
        return Flux.fromIterable(List.of("ANIRUDH", "SHILPII", "SHWETA", "MANU"))
                .transform(filterMap)
                .flatMap(FluxAndMonoGeneratorService::splitString) // s -> FluxAndMonoGeneratorService.splitString(s)
                .switchIfEmpty(defaultFlux) // if after transform and flatMap we get nothing then return default flux
//                .doOnNext(System.out::println)
                .log();
    }

    Flux<String> explore_concat() {
        // Used when we get elements from 2 data sources, and we wish to combine them sequentially and send it as Flux
        var flux1= Flux.just("A", "B", "C");
        var flux2= Flux.just("D", "E", "F");
        return Flux.concat(flux1, flux2).log(); //A, B, C, D, E, F
    }

     Flux<String> explore_concatWithFlux() {
         // Used when we get elements from 2 data sources, and we wish to combine them sequentially and send it as Flux
        var flux1= Flux.just("A", "B", "C");
        var flux2= Flux.just("D", "E", "F");
        return flux1.concatWith(flux2).log();
    }

    Flux<String> explore_concatWithMono() {
        // Used when we get single element from 2 data sources, and we wish to combine(in sequence) and send it as Flux
        var aMono= Mono.just("A");
        var bMono= Mono.just("D");
        return aMono.concatWith(bMono).log();
    }

    Flux<String> explore_merge() {
        // Used when we get elements from 2 data sources, and we wish to combine and send it as Flux
        // Timing is important because the elements emission will depend on it
        var flux1= Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        var flux2= Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));
        // interleaved merged sequence
        return Flux.merge(flux1, flux2).log(); // A, D, B, E, C, F
    }

    // Same as above "merge"
    Flux<String> explore_mergeWith() {
        // Used when we get elements from 2 data sources, and we wish to combine and send it as Flux
        // Timing is important because the elements emission will depend on it
        var flux1= Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        var flux2= Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));
        // Interleaved merged sequence
        return flux1.mergeWith(flux2).log(); // A, D, B, E, C, F
    }

    Flux<String> explore_mergeSequential() {
        // Used when we get elements from 2 data sources, and we wish to combine the result in a sequential manner
        // and send it as Flux. Elements are emitted with a delay but merged later sequentially
        var flux1= Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        var flux2= Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));
        // Publishers are subscribed at the same time(starts emitting elements same time but) "Merged sequence is sequential"
        return Flux.mergeSequential(flux1, flux2).log(); // A, B, C, D, E, F
    }

    Flux<String> explore_zip() {
        // Used when we wish to merge Publishers (can merge 2 to 8 publishers : Flux and Mono) into one
        // Publisher(s) are subscribed eagerly and waits for all Publishers involved in the transformation to emit one element
        // Continues until one Publisher sends onComplete event
        var flux1= Flux.just("A", "B", "C");
        var flux2= Flux.just("1", "2", "3");

        // Publishers are subscribed at the same time(starts emitting elements same time but) "Merged sequence is sequential"
        return Flux.zip(flux1, flux2, (pub1, pub2) -> pub1 + pub2)
                .log(); // Tuple => A1, B2, C3
    }

    Flux<String> explore_zipMore() {
        // Used when we wish to merge Publishers (can merge 2 to 8 publishers : Flux and Mono) into one
        // Publisher(s) are subscribed eagerly and waits for all Publishers involved in the transformation to emit one element
        // Continues until one Publisher sends onComplete event
        var flux1= Flux.just("A", "B", "C"); // Tuple is made for each flux
        var flux2= Flux.just("1", "2", "3");
        var flux3= Flux.just("D", "E", "F");
        var flux4= Flux.just("4", "5", "6");

        // Publishers are subscribed at the same time(starts emitting elements same time but) "Merged sequence is sequential"
        // Tuple element of each flux(n) object is taken and concatenated
        return Flux.zip(flux1, flux2, flux3, flux4)
                .map(elem -> elem.getT1() + elem.getT2() + elem.getT3() + elem.getT4())
                .log(); // A1D4, B2E5 C3F6
    }

    // ANIRUDH -> Flux(A, N, I, R, U, D, H, S, H, I, L, P, I, I)
    public static Flux<String> splitString(String name) {
        return Flux.fromArray(name.split(""));
    }

    // ANIRUDH -> Flux(A, N, I, R, U, D, H)
    public static Flux<String> splitString_withDelay(String name) {
//        var delay = new Random().nextInt(1000);
        var delay = 1000; // keeping delay fixed to compare flatMap vs concatMap
        // onNext signal of elements is delayed and continue in parallel
        return Flux.fromArray(name.split(""))
                .delayElements(Duration.ofMillis(delay));
    }

    // ANIRUDH -> Mono of List<String> = (A, N, I, R, U, D, H)
    public static Mono<List<String>> splitStringMono(String name) {
         return Mono.just(List.of(name.split("")));
    }
}
