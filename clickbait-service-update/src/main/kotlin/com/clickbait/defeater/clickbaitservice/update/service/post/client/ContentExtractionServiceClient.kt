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