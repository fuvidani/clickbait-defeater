package com.clickbait.defeater.clickbaitservice.read.service.score.client

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import org.springframework.http.MediaType
import reactor.core.publisher.Mono
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface IScoreServiceClient {

    @Headers(
        "Accept: ${MediaType.APPLICATION_JSON_UTF8_VALUE}",
        "Content-Type: ${MediaType.APPLICATION_JSON_UTF8_VALUE}"
    )
    @POST("predict")
    fun scorePostInstance(@Body instance: PostInstance): Mono<ClickBaitScore>
}