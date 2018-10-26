package com.clickbait.defeater.contentextraction.persistence.cache

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import reactor.core.publisher.Mono

/**
 * Interface for an abstract cache for [ContentWrapper] objects.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ContentCache {

    /**
     * Tries to retrieve the [ContentWrapper] object from the cache
     * with the provided `url` as the key.
     *
     * @param url the unique key associated with the required content
     * @return a Mono with the cached [ContentWrapper] object or
     * an empty Mono otherwise
     */
    fun tryAndGet(url: String): Mono<ContentWrapper>

    /**
     * Puts the provided [ContentWrapper] object into the cache
     * and returns an indicator flag about the operation's success.
     *
     * @param contentWrapper a valid content wrapper to cache
     * @return a Mono emitting `true` if the operation was successful,
     * otherwise `false`
     */
    fun put(contentWrapper: ContentWrapper): Mono<Boolean>
}