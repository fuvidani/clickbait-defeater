package com.clickbait.defeater.clickbaitservice.read.service.language.checker

import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import reactor.core.publisher.Mono

/**
 * Interface for language checking operations applied on [PostInstance]
 * objects.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface LanguageChecker {

    /**
     * Performs certain checks on the provided [PostInstance] object
     * and either emits the `instance` or an error as a result.
     *
     * @param instance a valid social media post instance
     * @return a Mono emitting the provided `instance` if all checks
     * passed, otherwise an error
     */
    fun check(instance: PostInstance): Mono<PostInstance>
}