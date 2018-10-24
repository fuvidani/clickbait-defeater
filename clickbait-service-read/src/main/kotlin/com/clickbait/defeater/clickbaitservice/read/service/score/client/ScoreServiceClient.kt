package com.clickbait.defeater.clickbaitservice.read.service.score.client

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import org.springframework.http.MediaType
import reactor.core.publisher.Mono
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * A declarative HTTP Score service client responsible for invoking a remote API.
 * The actual implementation is deferred.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ScoreServiceClient {

    /**
     * Makes a remote API call with the provided [PostInstance] and retrieves
     * its [ClickBaitScore] result in a non-blocking way via a [Mono] publisher.
     *
     * @param instance a valid social media post instance
     * @return a Mono containing the serialized [ClickBaitScore] object
     */
    @Headers(
        "Accept: ${MediaType.APPLICATION_JSON_UTF8_VALUE}",
        "Content-Type: ${MediaType.APPLICATION_JSON_UTF8_VALUE}"
    )
    @POST("predict")
    fun scorePostInstance(@Body instance: PostInstance): Mono<ClickBaitScore>
}