package com.clickbait.defeater.contentextraction.persistence.cache

import com.clickbait.defeater.contentextraction.model.ContentWrapper
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
class RedisContentCache(private val valueOperations: ReactiveValueOperations<String, ContentWrapper>) : ContentCache {

    override fun tryAndGet(url: String): Mono<ContentWrapper> {
        return valueOperations
            .get(url)
    }

    override fun put(contentWrapper: ContentWrapper): Mono<Boolean> {
        return valueOperations
            .set(contentWrapper.url, contentWrapper)
    }
}