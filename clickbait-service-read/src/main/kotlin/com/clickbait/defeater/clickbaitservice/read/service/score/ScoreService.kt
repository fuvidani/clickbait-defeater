package com.clickbait.defeater.clickbaitservice.read.service.score

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.score.client.IScoreServiceClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

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
class ScoreService(private val scoreServiceClient: IScoreServiceClient) : IScoreService {

    override fun scorePostInstance(instance: PostInstance): Mono<ClickBaitScore> {
        return Mono.create {
            try {
                val score = scoreServiceClient.scorePostInstance(instance).execute().body()
                it.success(score)
            } catch (e: Exception) {
                it.error(e)
            }
        }
    }
}