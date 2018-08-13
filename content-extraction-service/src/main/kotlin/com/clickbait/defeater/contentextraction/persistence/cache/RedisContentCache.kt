package com.clickbait.defeater.contentextraction.persistence.cache

import com.clickbait.defeater.contentextraction.model.Content
import com.clickbait.defeater.contentextraction.model.Contents
import com.clickbait.defeater.contentextraction.model.WebPage
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

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
class RedisContentCache(private val valueOperations: ReactiveValueOperations<String, Contents>) : ContentCache {

    override fun tryAndGet(webPage: WebPage): Flux<Content> {
        return valueOperations
            .get(webPage.url)
            .map { it.contents }
            .flatMapMany { Flux.fromIterable(it) }
    }

    override fun put(webPage: WebPage, contents: List<Content>): Flux<Content> {
        return valueOperations
            .set(webPage.url, Contents(contents))
            .flatMapMany { Flux.fromIterable(contents) }
    }
}