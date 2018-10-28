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

package com.clickbait.defeater.contentextraction.service.html.extractor.extractors.mercury.web.parser.client

import com.clickbait.defeater.contentextraction.model.MercuryApiResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * A declarative, reactive HTTP client interface responsible for invoking the
 * [Mercury Web Parser API](https://mercury.postlight.com/web-parser/).
 * The actual implementation is deferred.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
interface MercuryWebParserApiClient {

    /**
     * Retrieves multiple types of contents wrapped in a [MercuryApiResponse]
     * for an article defined by `url`.
     *
     * @param apiKey valid API-key for the web parser
     * @param url valid, absolute URL of the web article that should be parsed
     * @return a [Mono] emitting the result of the remote invocation
     */
    @Headers(
        "Accept: ${MediaType.APPLICATION_JSON_UTF8_VALUE}",
        "Content-Type: ${MediaType.APPLICATION_JSON_UTF8_VALUE}"
    )
    @GET("parser")
    fun getArticleContent(
        @Header("x-api-key") apiKey: String,
        @Query("url") url: String
    ): Mono<MercuryApiResponse>
}