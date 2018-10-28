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