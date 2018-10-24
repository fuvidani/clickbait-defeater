package com.clickbait.defeater.clickbaitservice.read.service.score.cache

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import reactor.core.publisher.Mono

/**
 * Interface for an abstract cache for [ClickBaitScore] objects.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ScoreCache {

    /**
     * Tries to retrieve the [ClickBaitScore] object from the cache,
     * which corresponds to the provided social media post instance.
     *
     * @param instance a valid social media post instance
     * @return a Mono with the cached [ClickBaitScore] object or
     * an empty Mono otherwise
     */
    fun tryAndGet(instance: PostInstance): Mono<ClickBaitScore>

    /**
     * Puts the provided [ClickBaitScore] object into the cache
     * and returns an indicator flag about the operation's success.
     *
     * @param score a valid clickbait score to cache
     * @return a Mono emitting `true` if the operation was successful,
     * otherwise `false`
     *
     */
    fun put(score: ClickBaitScore): Mono<Boolean>
}