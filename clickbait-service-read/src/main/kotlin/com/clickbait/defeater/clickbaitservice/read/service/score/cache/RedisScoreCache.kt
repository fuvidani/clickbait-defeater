package com.clickbait.defeater.clickbaitservice.read.service.score.cache

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Concrete implementation of the [ScoreCache] interface with Redis as the underlying
 * caching technology.
 *
 * @property valueOperations reactive Redis operations provided by the Spring Data framework
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class RedisScoreCache(private val valueOperations: ReactiveValueOperations<String, ClickBaitScore>) : ScoreCache {

    /**
     * Tries to retrieve the [ClickBaitScore] object from the cache,
     * which corresponds to the provided social media post instance.
     *
     * @param instance a valid social media post instance
     * @return a Mono with the cached [ClickBaitScore] object or
     * an empty Mono otherwise
     */
    override fun tryAndGet(instance: PostInstance): Mono<ClickBaitScore> {
        return valueOperations.get(instance.id)
    }

    /**
     * Puts the provided [ClickBaitScore] object into the cache
     * and returns an indicator flag about the operation's success.
     *
     * @param score a valid clickbait score to cache
     * @return a Mono emitting `true` if the operation was successful,
     * otherwise `false`
     *
     */
    override fun put(score: ClickBaitScore): Mono<Boolean> {
        return valueOperations.set(score.id, score)
    }
}