package com.clickbait.defeater.clickbaitservice.read.service.score.cache

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class RedisScoreCache(private val valueOperations: ReactiveValueOperations<String, ClickBaitScore>) : IScoreCache {

    override fun tryAndGet(instance: PostInstance): Mono<ClickBaitScore> {
        return valueOperations.get(instance.id)
    }

    override fun put(score: ClickBaitScore): Mono<Boolean> {
        return valueOperations.set(score.id, score)
    }
}