package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.internal.util.StringUtil;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @DisplayName("Names Flux")
    @Test
    void testNamesFlux() {
        // Arrange/Given

        // when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux();

        // Assert
        StepVerifier.create(namesFlux)
//                .expectNextCount(4) // no of elements we are expecting
//                .expectNext("Anirudh", "Shilpi", "Shweta", "Manu")
                .expectNext("ANIRUDH", "SHILPI") // sequence matters else test will fail
                .expectNextCount(2) // next 2 items "Shweta" & "Manu"
                .verifyComplete();

    }

    @DisplayName("Names Flux Map")
    @Test
    void testNamesFluxMap() {
        // Arrange/Given

        // when
        var namesFlux_Map = fluxAndMonoGeneratorService.namesFlux_Map();

        // Assert
        StepVerifier.create(namesFlux_Map)
//                .expectNextCount(4) // no of elements we are expecting
//                .expectNext("Anirudh", "Shilpi", "Shweta", "Manu")
                .expectNext("anirudh", "shilpi") // sequence matters else test will fail
                .expectNextCount(2) // next 2 items "Shweta" & "Manu"
                .verifyComplete();

    }

    @DisplayName("Names Flux Immutable")
    @Test
    void testNamesFluxImmutable() {
        // Arrange/Given

        // when
        // lower case map will not be applied
        var namesFlux_Map = fluxAndMonoGeneratorService.namesFlux_immutability();

        // Assert
        StepVerifier.create(namesFlux_Map)
//                .expectNextCount(4) // no of elements we are expecting
                .expectNext("ANIRUDH", "SHILPI", "SHWETA", "MANU")
                .verifyComplete();

    }

    @DisplayName("Names Flux Filter")
    @Test
    void testNamesFluxFilter() {
        // Arrange/Given
        int stringLength = 5;
        // when
        var namesFlux_Filter = fluxAndMonoGeneratorService.namesFlux_Filter(stringLength);

        // Assert : filtered "MANU" element
        StepVerifier.create(namesFlux_Filter)
                .expectNext("7-anirudh", "6-shilpi", "6-shweta") // sequence matters else test will fail
                .verifyComplete();
    }

    @DisplayName("Names Flux FlatMap")
    @Test
    void testNamesFluxFlatMap() {
        // Arrange/Given
        int stringLength = 7;
        // when
        var namesFlux_flatMap = fluxAndMonoGeneratorService.namesFlux_FlatMap(stringLength);

        // Assert : filtered "MANU" element
        StepVerifier.create(namesFlux_flatMap)
                .expectNext("a", "n", "i", "r", "u", "d", "h", "s", "h", "i", "l", "p", "i", "i") // sequence matters else test will fail
                .verifyComplete();
    }

    @DisplayName("Names Flux FlatMap Async")
    @Test
    void testNamesFluxFlatMapAsync() {
        // Arrange/Given
        int stringLength = 7;
        // when
        var namesFlux_flatMap_async = fluxAndMonoGeneratorService.namesFlux_FlatMap_Async(stringLength);

        // Assert : filtered "MANU" element AND Sequence of Flux elem. will be random since it is delayed by some milliseconds
        StepVerifier.create(namesFlux_flatMap_async)
                .expectNextCount(14)
//                .expectNext("a", "n", "i", "r", "u", "d", "h", "s","h","i","l","p","i","i") // sequence matters else test will fail
                .verifyComplete();
    }

    @DisplayName("Names Flux ConcatMap Async")
    @Test
    void testNamesFluxConcatMapAsync() {
        // Arrange/Given
        int stringLength = 7;
        // when
        var namesFlux_concatMap_async = fluxAndMonoGeneratorService.namesFlux_ConcatMap_Async(stringLength);

        // Assert : filtered "MANU" element AND Sequence of Flux elem. will be concatenated(in-order) even if it is delayed by some milliseconds
        // time taken : concatMap(slower) > flatMap(faster)
        StepVerifier.create(namesFlux_concatMap_async)
                .expectNext("a", "n", "i", "r", "u", "d", "h", "s", "h", "i", "l", "p", "i", "i") // sequence matters else test will fail
                .verifyComplete();
    }

    @DisplayName("Explore Concat")
    @Test
    void testExplore_Concat() {
        // Arrange/Given
        // When
        Flux<String> stringFlux = fluxAndMonoGeneratorService.explore_concat();

        // Assert
        StepVerifier.create(stringFlux)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @DisplayName("Explore Merge Sequential")
    @Test
    void testExplore_MergeSequential() {
        // Arrange/Given
        // When
        Flux<String> mergeSequentialFlux = fluxAndMonoGeneratorService.explore_mergeSequential();

        // Assert
        // Data will arrive in interleaved manner
        StepVerifier.create(mergeSequentialFlux)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @DisplayName("Explore Merge")
    @Test
    void testExplore_Merge() {
        // Arrange/Given
        // When
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.explore_merge();

        // Assert
        // Data will arrive in interleaved manner
        StepVerifier.create(mergeFlux)
                .expectNext("A","D","B","E","C","F")
                .verifyComplete();
    }

    @DisplayName("Explore Zip")
    @Test
    void testExplore_Zip() {
        // Arrange/Given
        // When
        Flux<String> zipFlux = fluxAndMonoGeneratorService.explore_zip();

        // Assert
        // Data will arrive in interleaved manner
        StepVerifier.create(zipFlux)
                .expectNext("A1","B2","C3")
                .verifyComplete();
    }

    @DisplayName("Explore ZipMore")
    @Test
    void testExplore_ZipMore() {
        // Arrange/Given
        // When
        Flux<String> zipFlux = fluxAndMonoGeneratorService.explore_zipMore();

        // Assert
        // Data will arrive in interleaved manner
        StepVerifier.create(zipFlux)
                .expectNext("A1D4","B2E5","C3F6")
                .verifyComplete();
    }

    @DisplayName("Names Flux Transform")
    @Test
    void testNamesFluxTransform() {
        // Arrange/Given
        int stringLength = 7;
        // when
        var namesFlux_transform = fluxAndMonoGeneratorService.namesFlux_Transform(stringLength);

        // Assert : filtered "MANU" element
        StepVerifier.create(namesFlux_transform)
                .expectNext("a", "n", "i", "r", "u", "d", "h", "s", "h", "i", "l", "p", "i", "i") // sequence matters else test will fail
                .verifyComplete();
    }

    @DisplayName("Names Flux Transform DefaultIfEmpty")
    @Test
    void testNamesFluxTransformDefaultIfEmpty() {
        // Arrange/Given
        int stringLength = 8;
        // when
        var namesFlux_transform = fluxAndMonoGeneratorService.namesFlux_Transform(stringLength);

        // Assert : filtered "MANU" element
        StepVerifier.create(namesFlux_transform)
                // .expectNext("a", "n", "i", "r", "u", "d", "h", "s", "h", "i", "l", "p", "i", "i") // sequence matters else test will fail
                .expectNext("default")
                .verifyComplete();
    }

    @DisplayName("Names Flux Transform SwitchIfEmpty")
    @Test
    void testNamesFluxTransformSwitchIfEmpty() {
        // Arrange/Given
        int stringLength = 8;
        // when
        var namesFlux_transform = fluxAndMonoGeneratorService.namesFlux_Transform_SwitchIfEmpty(stringLength);

        // Assert : filtered "MANU" element
        StepVerifier.create(namesFlux_transform)
                .expectNext("d", "e", "f", "a", "u", "l", "t", "t") // sequence matters else test will fail
                .verifyComplete();
    }

    @DisplayName("Names Mono Map")
    @Test
    void testNamesMono_map() {
        var stringLength = 7;
        var namesMono_map = fluxAndMonoGeneratorService.namesMono_map(stringLength);
        StepVerifier.create(namesMono_map)
                .expectNextCount(1)
                .verifyComplete();
    }

    @DisplayName("Names Mono FlatMap")
    @Test
    void testNamesMono_flatMap() {
        var stringLength = 7;
        var namesMono_flatMap = fluxAndMonoGeneratorService.namesMono_flatMap(stringLength);
        StepVerifier.create(namesMono_flatMap)
                .expectNext(List.of("A","N","I","R","U","D","H"))
                .verifyComplete();
    }

    @DisplayName("Names Mono FlatMapMany")
    @Test
    void testNamesMono_flatMapMany() {
        // Mono uses flatMapMany for transforming : if returning Flux from Mono pipeline

        var stringLength = 7;
        var namesMono_flatMapMany = fluxAndMonoGeneratorService.namesMono_flatMapMany(stringLength);
        StepVerifier.create(namesMono_flatMapMany)
                .expectNext("A","N","I","R","U","D","H")
                .verifyComplete();
    }
}