package com.clickbait.defeater.clickbaitservice.read.service.language.detector

import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import reactor.core.publisher.Mono

/**
 * Interface for language detection operations.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface LanguageDetector {

    /**
     * Analyzes the provided `instance`, tries to determine the language
     * of its contents and returns it extended by the correct language
     * attribute.
     *
     * @param instance a valid social media post instance s.t. its language
     * is questionable (either set or unknown)
     * @return a Mono emitting the same [PostInstance] object with the only
     * difference being in the `language` attribute
     */
    fun detect(instance: PostInstance): Mono<PostInstance>
}