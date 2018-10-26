/*
 * Clickbait-Defeater
 * Copyright (c) 2018. Daniel Füvesi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.clickbait.defeater.contentextraction.persistence.cache

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Concrete implementation of the [ContentCache] interface with Redis as the underlying
 * caching technology.
 *
 * @property valueOperations reactive Redis operations provided by the Spring Data framework
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class RedisContentCache(private val valueOperations: ReactiveValueOperations<String, ContentWrapper>) : ContentCache {

    /**
     * Tries to retrieve the [ContentWrapper] object from the cache
     * with the provided `url` as the key.
     *
     * @param url the unique key associated with the required content
     * @return a Mono with the cached [ContentWrapper] object or
     * an empty Mono otherwise
     */
    override fun tryAndGet(url: String): Mono<ContentWrapper> {
        return valueOperations
            .get(url)
    }

    /**
     * Puts the provided [ContentWrapper] object into the cache
     * and returns an indicator flag about the operation's success.
     *
     * @param contentWrapper a valid content wrapper to cache
     * @return a Mono emitting `true` if the operation was successful,
     * otherwise `false`
     */
    override fun put(contentWrapper: ContentWrapper): Mono<Boolean> {
        return valueOperations
            .set(contentWrapper.redirectUrl, contentWrapper)
    }
}