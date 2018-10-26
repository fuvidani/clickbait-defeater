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