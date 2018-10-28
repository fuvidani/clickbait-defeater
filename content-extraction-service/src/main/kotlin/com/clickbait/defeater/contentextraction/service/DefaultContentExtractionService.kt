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

package com.clickbait.defeater.contentextraction.service

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import com.clickbait.defeater.contentextraction.model.WebPage
import com.clickbait.defeater.contentextraction.persistence.ContentDataStore
import com.clickbait.defeater.contentextraction.service.handler.ContentExtractionHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Concrete implementation of the [ContentExtractionService] interface using
 * a [ContentDataStore] to store already extracted contents thus improving
 * overall performance by not invoking the [ContentExtractionHandler] for
 * the same input multiple times.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property handler a reactive [ContentExtractionHandler] implementation
 * responsible for handling the actual extraction process
 * @property store a reactive [ContentDataStore] for persisting and
 * retrieving extracted contents
 */
@Component
class DefaultContentExtractionService(
    private val handler: ContentExtractionHandler,
    private val store: ContentDataStore
) : ContentExtractionService {

    /**
     * Extracts as much structured content as possible from the
     * provided `webPage` and returns it as a [ContentWrapper]
     * published by a [Mono].
     *
     * @param webPage a valid [WebPage] object as input, describing
     * the web page of which the contents should be extracted
     * @return a Mono emitting the extracted contents
     */
    override fun extractContent(webPage: WebPage): Mono<ContentWrapper> {
        return store
            .findById(webPage.url)
            .switchIfEmpty(
                Mono.defer {
                    handler.extract(webPage)
                        .flatMap { store.save(it) }
                }
            )
    }
}