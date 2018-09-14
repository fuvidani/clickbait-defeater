package com.clickbait.defeater.clickbaitservice.update.persistence

import com.clickbait.defeater.clickbaitservice.update.model.MultiplePostInstanceJudgments
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
interface JudgmentsRepository {

    @Headers(
        "Accept: ${MediaType.APPLICATION_JSON_UTF8_VALUE}",
        "Content-Type: ${MediaType.APPLICATION_JSON_UTF8_VALUE}"
    )
    @POST("train")
    fun saveAll(@Body multipleJudgments: MultiplePostInstanceJudgments): Mono<Void>
}