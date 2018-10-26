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

package com.clickbait.defeater.contentextraction.persistence

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import reactor.core.publisher.Mono

/**
 * Interface describing the persistence layer operations of
 * [ContentWrapper] objects.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ContentDataStore {

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
    fun findById(url: String): Mono<ContentWrapper>

    /**
     * Saves the provided `contentWrapper` into the data-store and
     * returns the saved object in a [Mono]. For new objects this
     * corresponds to an insert operation, otherwise it is an update
     * one.
     *
     * @param contentWrapper valid content wrapper object to save
     * @return a Mono emitting the persisted object
     */
    fun save(contentWrapper: ContentWrapper): Mono<ContentWrapper>
}