package com.clickbait.defeater.contentextraction.persistence

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.persistence.cache.ContentCache
import com.clickbait.defeater.contentextraction.persistence.repository.ContentRepository
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
class DefaultContentDataStore(
    private val repository: ContentRepository,
    private val cache: ContentCache
) : ContentDataStore {

    override fun findById(url: String): Mono<ContentWrapper> {
        return cache.tryAndGet(url)
            .switchIfEmpty(Mono.defer { repository.findById(url) })
    }

    override fun save(contentWrapper: ContentWrapper): Mono<ContentWrapper> {
        return cache.put(contentWrapper)
            .then(repository.save(contentWrapper))
    }
}