package com.clickbait.defeater.clickbaitservice.read.service.score

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import reactor.core.publisher.Mono

/**
 * Interface for scoring operations.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ScoreService {

    /**
     * Analyzes the provided social media post instance and determines its clickbait
     * score, i.e. how "clickbaity" the post might be.
     *
     * @param instance a valid social media post instance
     * @return a Mono of a valid [ClickBaitScore] containing the determined score
     */
    fun scorePostInstance(instance: PostInstance): Mono<ClickBaitScore>
}