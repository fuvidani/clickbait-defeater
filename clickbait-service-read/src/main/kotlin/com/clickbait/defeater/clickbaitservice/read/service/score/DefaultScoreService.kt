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

package com.clickbait.defeater.clickbaitservice.read.service.score

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.exception.ClickBaitReadServiceException
import com.clickbait.defeater.clickbaitservice.read.service.score.client.ScoreServiceClient
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Implementation of the [ScoreService] interface.
 *
 * @property scoreServiceClient client interface for communicating with the remote service
 * where the actual scoring process is executed
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class DefaultScoreService(private val scoreServiceClient: ScoreServiceClient) : ScoreService {

    /**
     * Analyzes the provided social media post instance and determines its clickbait
     * score, i.e. how "clickbaity" the post might be.
     *
     * @param instance a valid social media post instance
     * @return a Mono of a valid [ClickBaitScore] containing the determined score
     */
    override fun scorePostInstance(instance: PostInstance): Mono<ClickBaitScore> {
        return scoreServiceClient
            .scorePostInstance(instance)
            .onErrorMap {
                logger.error("Remote score service cannot be reached", it)
                ClickBaitReadServiceException("The post could not be scored.", HttpStatus.INTERNAL_SERVER_ERROR)
            }
            .map { ClickBaitScore(it.id, it.clickbaitScore, instance.language) }
    }

    companion object : KLogging()
}