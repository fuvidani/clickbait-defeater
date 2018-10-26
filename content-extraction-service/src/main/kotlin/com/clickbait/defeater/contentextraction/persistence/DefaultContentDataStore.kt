package com.clickbait.defeater.contentextraction.persistence

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.persistence.cache.ContentCache
import com.clickbait.defeater.contentextraction.persistence.repository.ContentRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Concrete implementation of the [ContentDataStore] interface.
 * This persistence implementation consists of two layers: a cache
 * on top of a database repository. The operations first use the cache
 * for look-ups and insertions and only then the repository gets invoked.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property repository a reactive [ContentRepository] implementation for
 * persisting [ContentWrapper] objects
 * @property cache a reactive [ContentCache] implementation for in-memory
 * caching of [ContentWrapper] objects
 */
@Component
class DefaultContentDataStore(
    private val repository: ContentRepository,
    private val cache: ContentCache
) : ContentDataStore {

    /**
     * Performs a look-up in the data-store for a [ContentWrapper]
     * object with the provided `url` as its ID and publishes it
     * via a [Mono].
     *
     * @param url the key of the required [ContentWrapper]
     * @return a Mono emitting the found content wrapper or an empty
     * Mono if the data-store doesn't contain any wrapper with the
     * given `url`
     */
    override fun findById(url: String): Mono<ContentWrapper> {
        return cache
            .tryAndGet(url)
            .switchIfEmpty(Mono.defer { repository.findById(url) })
    }

    /**
     * Saves the provided `contentWrapper` into the data-store and
     * returns the saved object in a [Mono]. For new objects this
     * corresponds to an insert operation, otherwise it is an update
     * one.
     *
     * @param contentWrapper valid content wrapper object to save
     * @return a Mono emitting the persisted object
     */
    override fun save(contentWrapper: ContentWrapper): Mono<ContentWrapper> {
        return cache
            .put(contentWrapper)
            .then(repository.save(contentWrapper))
    }
}