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
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
interface MercuryWebParserApiClient {

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