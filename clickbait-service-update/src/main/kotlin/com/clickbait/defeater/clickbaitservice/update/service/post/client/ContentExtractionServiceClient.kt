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

package com.clickbait.defeater.clickbaitservice.update.service.post.client

import com.clickbait.defeater.clickbaitservice.update.model.content.ContentWrapper
import org.springframework.http.MediaType
import reactor.core.publisher.Mono
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * A declarative HTTP Content-Extraction Service client responsible for invoking
 * a remote API. The actual implementation is deferred.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ContentExtractionServiceClient {

    /**
     * Executes a remote API call with the provided `url` and retrieves
     * the [ContentWrapper] result in a non-blocking way via a [Mono] publisher.
     *
     * @param url absolute URL of the web page for which the contents should be
     * extracted
     * @return a Mono containing the serialized [ContentWrapper] object
     */
    @Headers(
        "Accept: ${MediaType.APPLICATION_JSON_UTF8_VALUE}",
        "Content-Type: ${MediaType.APPLICATION_JSON_UTF8_VALUE}"
    )
    @GET("content")
    fun extractContent(@Query("url") url: String): Mono<ContentWrapper>
}