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