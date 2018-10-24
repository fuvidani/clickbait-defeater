package com.clickbait.defeater.clickbaitservice.read.service

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import reactor.core.publisher.Mono

/**
 * Interface for top-level functionality of the Read-Service.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ClickBaitReadService {

    /**
     * Determines the clickbait score of the provided social media post instance.
     *
     * This operation guarantees a valid [ClickBaitScore] object or an appropriate
     * exception if an error occurs.
     *
     * @param instance a valid social media post instance
     * @return a Mono publisher with the clickbait score object corresponding to the
     * provided instance
     */
    fun scorePostInstance(instance: PostInstance): Mono<ClickBaitScore>
}