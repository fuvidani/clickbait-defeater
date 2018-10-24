package com.clickbait.defeater.clickbaitservice.update.persistence

import com.clickbait.defeater.clickbaitservice.update.model.MultiplePostInstanceJudgments
import org.springframework.http.MediaType
import reactor.core.publisher.Mono
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Reactive repository interface for [MultiplePostInstanceJudgments] objects.
 * For the client this acts just like any other repository with limited
 * functionality, in reality the objects are persisted to a remote API.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface JudgmentsRepository {

    /**
     * Saves the provided `multipleJudgments` to the JudgmentsRepository and
     * returns an empty mono.
     *
     * @param multipleJudgments valid instance of [MultiplePostInstanceJudgments]
     * @return an empty Mono
     */
    @Headers(
        "Accept: ${MediaType.APPLICATION_JSON_UTF8_VALUE}",
        "Content-Type: ${MediaType.APPLICATION_JSON_UTF8_VALUE}"
    )
    @POST("train")
    fun saveAll(@Body multipleJudgments: MultiplePostInstanceJudgments): Mono<Void>
}