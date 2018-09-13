package com.clickbait.defeater.clickbaitservice.update.service.post.client

import com.clickbait.defeater.clickbaitservice.update.model.content.ContentWrapper
import org.springframework.http.MediaType
import reactor.core.publisher.Mono
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ContentExtractionServiceClient {

    @Headers(
        "Accept: ${MediaType.APPLICATION_JSON_UTF8_VALUE}",
        "Content-Type: ${MediaType.APPLICATION_JSON_UTF8_VALUE}"
    )
    @GET("content")
    fun extractContent(@Query("url") url: String): Mono<ContentWrapper>
}